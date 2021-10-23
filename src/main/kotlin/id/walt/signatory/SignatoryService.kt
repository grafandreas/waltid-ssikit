package id.walt.signatory

import id.walt.servicematrix.BaseService
import id.walt.servicematrix.ServiceConfiguration
import id.walt.servicematrix.ServiceProvider
import id.walt.services.context.WaltContext
import id.walt.services.vc.JsonLdCredentialService
import id.walt.services.vc.JwtCredentialService
import id.walt.vclib.Helpers.encode
import id.walt.vclib.Helpers.toCredential
import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.templates.VcTemplateManager
import java.util.*

// JWT are using the IANA types of signatures: alg=EdDSA oder ES256 oder ES256K oder RS256
//enum class ProofType {
//    JWT_EdDSA,
//    JWT_ES256,
//    JWT_ES256K,
//    LD_PROOF_Ed25519Signature2018,
//    LD_PROOF_EcdsaSecp256k1Signature2019
//}

// Assuming the detailed algorithm will be derived from the issuer key algorithm
enum class ProofType {
    JWT,
    LD_PROOF
}

data class ProofConfig(
    val issuerDid: String,
    val subjectDid: String? = null,
    val verifierDid: String? = null,
    val issuerVerificationMethod: String? = null, // DID URL that defines key ID; if null the issuers' default key is used
    val proofType: ProofType = ProofType.LD_PROOF,
    val domain: String? = null,
    val nonce: String? = null,
    val proofPurpose: String? = null,
    val credentialId: String? = null,
    val issueDate: Date? = null, // issue date from json-input or current system time if null
    val validDate: Date? = null, // valid date from json-input or current system time if null
    val expirationDate: Date? = null,
    val dataProviderIdentifier: String? = null // may be used for mapping data-sets from a custom data-provider
)

data class SignatoryConfig(
    val proofConfig: ProofConfig
) : ServiceConfiguration

interface ISignatory {

    fun issue(templateId: String, config: ProofConfig): String

}

abstract class Signatory : BaseService(), ISignatory {
    override val implementation: Signatory get() = serviceImplementation()

    companion object : ServiceProvider {
        override fun getService() = object : Signatory() {}
    }

    override fun issue(templateId: String, config: ProofConfig): String = implementation.issue(templateId, config)
    open fun listTemplates(): List<String> = implementation.listTemplates()
    open fun loadTemplate(templateId: String): VerifiableCredential = implementation.loadTemplate(templateId)
}

class WaltSignatory(configurationPath: String) : Signatory() {

    private val VC_GROUP = "signatory"
    override val configuration: SignatoryConfig = fromConfiguration(configurationPath)

    override fun issue(templateId: String, config: ProofConfig): String {

        // TODO: load proof-conf from signatory.conf and optionally substitute values on request basis
        val vcTemplate = VcTemplateManager.loadTemplate(templateId)
        val configDP = when (config.credentialId.isNullOrBlank()) {
            true -> ProofConfig(
                config.issuerDid, config.subjectDid, null, config.issuerVerificationMethod, config.proofType,
                config.domain, config.nonce, config.proofPurpose,
                "identity#${templateId}#${UUID.randomUUID()}",
                config.issueDate, config.expirationDate
            )
            else -> config
        }
        val dataProvider = DataProviderRegistry.getProvider(vcTemplate::class) // vclib.getUniqueId(vcTemplate)
        val vcRequest = dataProvider.populate(vcTemplate, configDP)

        val signedVc = when (config.proofType) {
            ProofType.LD_PROOF -> JsonLdCredentialService.getService().sign(vcRequest.encode(), config)
            ProofType.JWT -> JwtCredentialService.getService().sign(vcRequest.encode(), config)
        }
        WaltContext.vcStore.storeCredential(configDP.credentialId!!, signedVc.toCredential(), VC_GROUP)
        return signedVc
    }

    override fun listTemplates(): List<String> = VcTemplateManager.getTemplateList()

    override fun loadTemplate(templateId: String): VerifiableCredential = VcTemplateManager.loadTemplate(templateId)

}
