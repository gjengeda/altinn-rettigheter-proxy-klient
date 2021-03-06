package no.nav.arbeidsgiver.altinnrettigheter.proxy.klient

import com.github.tomakehurst.wiremock.client.MappingBuilder
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient.Companion.CONSUMER_ID_HEADER_NAME
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.AltinnrettigheterProxyKlient.Companion.CORRELATION_ID_HEADER_NAME
import no.nav.arbeidsgiver.altinnrettigheter.proxy.klient.error.ProxyError
import org.apache.http.HttpStatus

class AltinnrettigheterProxyKlientIntegrationTestUtils {

    companion object {
        const val NON_EMPTY_STRING_REGEX = "^(?!\\s*\$).+"

        fun `altinn-rettigheter-proxy returnerer 200 OK og en liste med to AltinnReportee`(
                serviceCode: String,
                serviceEdition: String
        ): MappingBuilder {

            return get(urlPathEqualTo("/proxy/organisasjoner"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withHeader("Authorization", matching(NON_EMPTY_STRING_REGEX))
                    .withHeader(CORRELATION_ID_HEADER_NAME, matching(NON_EMPTY_STRING_REGEX))
                    .withHeader(CONSUMER_ID_HEADER_NAME, matching(NON_EMPTY_STRING_REGEX))

                    .withQueryParams(mapOf(
                            "serviceCode" to equalTo(serviceCode),
                            "serviceEdition" to equalTo(serviceEdition)
                    ))
                    .willReturn(`200 response med en liste av to reportees`())
        }

        fun `altinn-rettigheter-proxy returnerer en feil av type 'httpStatus' med 'kilde' og 'melding' i response body`(
                serviceCode: String,
                serviceEdition: String,
                httpStatusKode: Int,
                kilde: ProxyError.Kilde,
                melding: String
        ): MappingBuilder {
            return get(urlPathEqualTo("/proxy/organisasjoner"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withQueryParams(mapOf(
                            "serviceCode" to equalTo(serviceCode),
                            "serviceEdition" to equalTo(serviceEdition)
                    ))
                    .willReturn(aResponse()
                            .withStatus(httpStatusKode)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{" +
                                    "\"origin\": \"${kilde.verdi}\"," +
                                    "\"message\": \"${melding}\"}"
                            )
                    )
        }

        fun `altinn-rettigheter-proxy returnerer 500 uhåndtert feil`(
                serviceCode: String,
                serviceEdition: String
        ): MappingBuilder {
            return get(urlPathEqualTo("/proxy/organisasjoner"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withQueryParams(mapOf(
                            "serviceCode" to equalTo(serviceCode),
                            "serviceEdition" to equalTo(serviceEdition)
                    ))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                            .withHeader("Content-Type", "application/json")
                            .withBody("{" +
                                    "\"status\": \"500\"," +
                                    "\"message\": \"Internal Server Error\"}"
                            )
                    )
        }

        fun `altinn returnerer 200 OK og en liste med to AltinnReportee`(serviceCode: String, serviceEdition: String): MappingBuilder {
            return get(urlPathEqualTo("/altinn/ekstern/altinn/api/serviceowner/reportees"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withQueryParams(mapOf(
                            "serviceCode" to equalTo(serviceCode),
                            "serviceEdition" to equalTo(serviceEdition)
                    ))
                    .willReturn(`200 response med en liste av to reportees`())
        }

        fun `200 response med en liste av to reportees`(): ResponseDefinitionBuilder? {
            return aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("[" +
                            "    {" +
                            "        \"Name\": \"BALLSTAD OG HAMARØY\"," +
                            "        \"Type\": \"Business\"," +
                            "        \"ParentOrganizationNumber\": \"811076112\"," +
                            "        \"OrganizationNumber\": \"811076732\"," +
                            "        \"OrganizationForm\": \"BEDR\"," +
                            "        \"Status\": \"Active\"" +
                            "    }," +
                            "    {" +
                            "        \"Name\": \"BALLSTAD OG HORTEN\"," +
                            "        \"Type\": \"Enterprise\"," +
                            "        \"ParentOrganizationNumber\": null," +
                            "        \"OrganizationNumber\": \"811076112\"," +
                            "        \"OrganizationForm\": \"AS\"," +
                            "        \"Status\": \"Active\"" +
                            "    }" +
                            "]")
        }

        fun `altinn returnerer 400 Bad Request`(
                melding: String,
                serviceCode:
                String, serviceEdition: String
        ): MappingBuilder {
            return get(urlPathEqualTo("/altinn/ekstern/altinn/api/serviceowner/reportees"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withQueryParams(mapOf(
                            "serviceCode" to equalTo(serviceCode),
                            "serviceEdition" to equalTo(serviceEdition)
                    ))
                    .willReturn(aResponse()
                            .withStatus(400)
                            .withHeader("Content-Type", "application/json")
                            .withBody("\"message\": \"${melding}\"")
                    )
        }


        fun `altinn-rettigheter-proxy mottar riktig request`(
                serviceCode: String,
                serviceEdition: String
        ): RequestPatternBuilder {
            return getRequestedFor(urlPathEqualTo("/proxy/organisasjoner"))
                    .withHeader("Accept", containing("application/json"))
                    .withHeader("Accept", equalTo("application/json"))
                    .withHeader("Authorization", matching(NON_EMPTY_STRING_REGEX))
                    .withHeader(CORRELATION_ID_HEADER_NAME, matching(NON_EMPTY_STRING_REGEX))
                    .withHeader(CONSUMER_ID_HEADER_NAME, matching(NON_EMPTY_STRING_REGEX))
                    .withQueryParam("serviceCode", equalTo(serviceCode))
                    .withQueryParam("serviceEdition", equalTo(serviceEdition))
        }

        fun `altinn mottar riktig request`(
                serviceCode: String,
                serviceEdition: String,
                subject: String
        ): RequestPatternBuilder {
            return getRequestedFor(urlPathEqualTo("/altinn/ekstern/altinn/api/serviceowner/reportees"))
                    .withHeader("Accept", containing("application/json"))
                    .withHeader(CORRELATION_ID_HEADER_NAME, matching(NON_EMPTY_STRING_REGEX))
                    .withoutHeader("Authorization")
                    .withQueryParam("ForceEIAuthentication", equalTo(""))
                    .withQueryParam("subject", equalTo(subject))
                    .withQueryParam("serviceCode", equalTo(serviceCode))
                    .withQueryParam("serviceEdition", equalTo(serviceEdition))
        }
    }
}