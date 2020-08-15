package com.porterhead.sms

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.restassured.RestAssured
import io.restassured.response.ResponseBody
import java.lang.reflect.Field
import java.util.*

open class WiremockTestResource : QuarkusTestResourceLifecycleManager {

    lateinit var wireMockServer: WireMockServer
    lateinit var keyPair: RSAKey

    override fun start(): MutableMap<String, String> {
        wireMockServer = WireMockServer(WireMockConfiguration().dynamicPort())
        wireMockServer.start()
        keyPair = generatePrivateKey()
        postStubMapping(oidcConfigurationStub())
        postStubMapping(publicKeysStub(keyPair.toPublicJWK().toJSONString()))
        return mapOf("quarkus.oidc.auth-server-url" to wireMockServer.baseUrl() + "/mock-server",
                    "wiremock.url" to wireMockServer.baseUrl()).toMutableMap()
    }

    /**
     * Generate a key pair for signing and verifying JWTs
     */
    fun generatePrivateKey() :RSAKey {
        return  RSAKeyGenerator(2048)
                .keyID("123").keyUse(KeyUse.SIGNATURE)
                .generate()
    }

    /**
     * Mimic an OIDC server by generating a JWT using the private key
     */
    fun generateJWT(keyPair: RSAKey): String {
        val signer: JWSSigner = RSASSASigner(keyPair.toRSAKey())
        // Prepare JWT with claims set
        val claimsSet = JWTClaimsSet.Builder()
                .subject("backend-service")
                .issuer("https://example.com")
                .expirationTime(Date(Date().time + 60 * 1000))
                .build()
        val signedJWT = SignedJWT(
                JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyPair.keyID).type(JOSEObjectType.JWT).build(),
                claimsSet)
        // Compute the RSA signature
        signedJWT.sign(signer)
        return signedJWT.serialize()
    }

    override fun stop() {
        wireMockServer.stop()
    }


    /**
     * Post a stub mapping to wiremock
     *
     * @param request the json stub
     * @return the response of the new mapping
     */
    private fun postStubMapping(request: String): ResponseBody<*> {
        RestAssured.baseURI = wireMockServer.baseUrl()
        return RestAssured.given()
                .body(request)
                .post("/__admin/mappings")
                .then()
                .statusCode(201)
                .extract()
                .response()
                .body()
    }

    /**
     * Mock the response from an OIDC server's well-known configuration endpoint
     * This response is copied from an Okta server
     * The important property is jwks_uri which will be used to fetch the public key to verify JWTs
     */
    private fun oidcConfigurationStub(): String {
        return """
        {
            "name": "oidc_configuration",
            "request": {
                 "method": "GET",
                 "url": "/mock-server/.well-known/openid-configuration"
            },
            "response": {
                 "status": 200,
                 "headers": {
                     "Content-Type": "application/json;charset=UTF-8"
                 },
            "jsonBody": {
                 	"issuer": "${wireMockServer.baseUrl()}/mock-server",
	                "authorization_endpoint": "${wireMockServer.baseUrl()}/v1/authorize",
	                "token_endpoint": "${wireMockServer.baseUrl()}/v1/token",
	                "userinfo_endpoint": "${wireMockServer.baseUrl()}/v1/userinfo",
	                "registration_endpoint": "${wireMockServer.baseUrl()}/v1/clients",
	                "jwks_uri": "${wireMockServer.baseUrl()}/v1/keys",
	                "response_types_supported": ["code", "id_token", "code id_token", "code token", "id_token token", "code id_token token"],
	                "response_modes_supported": ["query", "fragment", "form_post", "okta_post_message"],
	                "grant_types_supported": ["authorization_code", "implicit", "refresh_token", "password"],
	                "subject_types_supported": ["public"],
	                "id_token_signing_alg_values_supported": ["RS256"],
	                "scopes_supported": ["sms", "openid", "profile", "email", "address", "phone", "offline_access"],
	                "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt", "none"],
	                "claims_supported": ["iss", "ver", "sub", "aud", "iat", "exp", "jti", "auth_time", "amr", "idp", "nonce", "name", "nickname", "preferred_username", "given_name", "middle_name", "family_name", "email", "email_verified", "profile", "zoneinfo", "locale", "address", "phone_number", "picture", "website", "gender", "birthdate", "updated_at", "at_hash", "c_hash"],
	                "code_challenge_methods_supported": ["S256"],
	                "introspection_endpoint": "${wireMockServer.baseUrl()}/v1/introspect",
	                "introspection_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt", "none"],
	                "revocation_endpoint": "${wireMockServer.baseUrl()}/v1/revoke",
	                "revocation_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt", "none"],
	                "end_session_endpoint": "${wireMockServer.baseUrl()}/v1/logout",
	                "request_parameter_supported": true,
	                "request_object_signing_alg_values_supported": ["HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "ES256", "ES384", "ES512"]
            }
        }
        }
        """
    }

    /**
     * Stub the response from an OIDC server to fetch the public keys
     */
    private fun publicKeysStub(keys: String): String {
        return """
            {
            "name": "public_keys_stub",
            "request": {
                 "method": "GET",
                 "url": "/v1/keys"
            },
            "response": {
                 "status": 200,
                 "headers": {
                     "Content-Type": "application/json;charset=UTF-8"
                 },
             "jsonBody":
                 {"keys":[$keys]}
            }
        }
        """
    }

    /**
     * Inject the properties from this class into test sub-class instance
     */
    override fun inject(testInstance: Any) {
        var c: Class<*> = testInstance::class.java
        while (c != Object::class.java) {
            for (f: Field in c.declaredFields) {
                when {
                    WireMockServer::class.java.isAssignableFrom(f.type) -> {
                        f.isAccessible = true
                        f.set(testInstance, wireMockServer)
                    }
                    RSAKey::class.java.isAssignableFrom(f.type) -> {
                        f.isAccessible = true
                        f.set(testInstance, keyPair)
                    }
                }
            }
            c = c.superclass
        }
    }
}
