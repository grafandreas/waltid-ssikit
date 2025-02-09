package id.walt.test

import id.walt.signatory.ProofConfig
import id.walt.signatory.ProofType
import id.walt.signatory.SignatoryDataProvider
import id.walt.signatory.dataproviders.DefaultDataProvider
import id.walt.vclib.credentials.VerifiableDiploma
import id.walt.vclib.credentials.VerifiableId
import id.walt.vclib.model.Proof
import id.walt.vclib.model.VerifiableCredential

class DummySignatoryDataProvider : SignatoryDataProvider {
    override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        if (template is VerifiableId) {
            DefaultDataProvider.populate(template, proofConfig)
            template.evidence!![0].id = "Dummy test value (waiting for EBSI schema update)"
            if (proofConfig.proofType == ProofType.JWT) template.proof = Proof(
                type = "Ed25519Signature2018", // Dummy test value (waiting for EBSI schema update)
                created = "2021-10-28T16:20:00Z", // Dummy test value (waiting for EBSI schema update)
                creator = "Dummy test value (waiting for EBSI schema update)",
                domain = "Dummy test value (waiting for EBSI schema update)",
                proofPurpose = "Dummy test value (waiting for EBSI schema update)",
                verificationMethod = "Dummy test value (waiting for EBSI schema update)",
                jws = "Dummy test value (waiting for EBSI schema update)",
                nonce = "Dummy test value (waiting for EBSI schema update)"
            )
        }
        if (template is VerifiableDiploma) {
            DefaultDataProvider.populate(template, proofConfig)
            template.credentialSubject!!.awardingOpportunity!!.awardingBody.eidasLegalIdentifier =
                "Dummy test value (waiting for EBSI schema update)"
            if (proofConfig.proofType == ProofType.JWT) template.proof = Proof(
                type = "Ed25519Signature2018", // Dummy test value (waiting for EBSI schema update)
                created = "2021-10-28T16:20:00Z", // Dummy test value (waiting for EBSI schema update)
                creator = "Dummy test value (waiting for EBSI schema update)",
                domain = "Dummy test value (waiting for EBSI schema update)",
                proofPurpose = "Dummy test value (waiting for EBSI schema update)",
                verificationMethod = "Dummy test value (waiting for EBSI schema update)",
                jws = "Dummy test value (waiting for EBSI schema update)",
                nonce = "Dummy test value (waiting for EBSI schema update)"
            )
        }
        return template
    }
}
