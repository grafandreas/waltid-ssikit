package id.walt.signatory

import id.walt.vclib.model.VerifiableCredential
import id.walt.vclib.vclist.VerifiableDiploma
import id.walt.vclib.vclist.VerifiableId
import java.util.*

object CLIDataProviders {
    fun getCLIDataProviderFor(templateId: String): SignatoryDataProvider? {
        return when(templateId) {
            "VerifiableDiploma" -> VerifiableDiplomaCLIDataProvider()
            "VerifiableId" -> VerifiableIDCLIDataProvider()
            else -> null
        }
    }
}

abstract class CLIDataProvider : SignatoryDataProvider {
    fun prompt(prompt: String, default: String?): String? {
        print("$prompt [$default]: ")
        val input = readLine()
        return when(input.isNullOrBlank()) {
            true -> default
            else -> input
        }
    }

    fun promptInt(prompt: String, default: Int?): Int {
        val str = prompt(prompt, default.let { it.toString() })
        return str.let { Integer.parseInt(it) }
    }
}

class VerifiableDiplomaCLIDataProvider : CLIDataProvider() {
    override fun populate(template: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        template as VerifiableDiploma

        template.apply {
            id = proofConfig.credentialId ?: "education#higherEducation#${UUID.randomUUID()}"
            issuer = proofConfig.issuerDid
            if (proofConfig.issueDate != null) issuanceDate = dateFormat.format(proofConfig.issueDate)
            if (proofConfig.expirationDate != null) expirationDate = dateFormat.format(proofConfig.expirationDate)
            validFrom = issuanceDate

            credentialSubject!!.apply {
                id = proofConfig.subjectDid

                println()
                println("Subject personal data, ID: ${proofConfig.subjectDid}")
                println("----------------------")
                identifier = prompt("Identifier", identifier)
                familyName = prompt("Family name", familyName)
                givenNames = prompt("Given names", givenNames)
                dateOfBirth = prompt("Date of birth", dateOfBirth)

                println()
                println("Awarding Opportunity")
                println("----------------------")
                awardingOpportunity!!.apply {
                    id = prompt("Opportunity ID", id) ?: ""
                    identifier = prompt("Identifier", identifier) ?: ""
                    location = prompt("Location", location) ?: ""
                    startedAtTime = prompt("Started at", startedAtTime) ?: ""
                    endedAtTime = prompt("Ended at", endedAtTime) ?: ""

                    println()
                    println("Awarding Body, ID: ${proofConfig.issuerDid}")
                    awardingBody.apply {
                        id = proofConfig.issuerDid
                        preferredName = prompt("Preferred name", preferredName) ?: ""
                        homepage = prompt("Homepage", homepage) ?: ""
                        registration = prompt("Registration", registration) ?: ""
                    }
                }

                println()
                println("Grading scheme")
                println("----------------------")
                gradingScheme?.apply {
                    id = prompt("Grading Scheme ID", id) ?: ""
                    title = prompt("Title", title) ?: ""
                }

                println()
                println("Learning Achievement")
                println("----------------------")
                learningAchievement?.apply {
                    id = prompt("Learning achievement ID", id) ?: ""
                    title = prompt("Title", title) ?: ""
                    description = prompt("Description", description) ?: ""
                    additionalNote = listOf(prompt("Additional note", additionalNote?.get(0)) ?: "")
                }

                println()
                println("Learning Specification")
                println("----------------------")
                learningSpecification?.apply {
                    id = prompt("Learning specification ID", id) ?: ""
                    ectsCreditPoints = promptInt("ECTS credit points", ectsCreditPoints)
                    eqfLevel = promptInt("EQF Level", eqfLevel)
                    iscedfCode = listOf(prompt("ISCEDF Code", iscedfCode[0]) ?: "")
                    nqfLevel = listOf(prompt("NQF Level", nqfLevel[0]) ?: "")
                }
            }
        }

        return template
    }
}

class VerifiableIDCLIDataProvider : CLIDataProvider() {
    override fun populate(vc: VerifiableCredential, proofConfig: ProofConfig): VerifiableCredential {
        vc as VerifiableId
        vc.id = proofConfig.credentialId ?: "education#higherEducation#${UUID.randomUUID()}"
        vc.issuer = proofConfig.issuerDid
        if (proofConfig.issueDate != null) vc.issuanceDate = dateFormat.format(proofConfig.issueDate)
        if (proofConfig.expirationDate != null) vc.expirationDate = dateFormat.format(proofConfig.expirationDate)
        vc.validFrom = vc.issuanceDate
        vc.credentialSubject!!.id = proofConfig.subjectDid

        println()
        println("Subject personal data, ID: ${proofConfig.subjectDid}")
        println("----------------------")
        vc.credentialSubject!!.firstName = prompt("First name", vc.credentialSubject!!.firstName)
        vc.credentialSubject!!.familyName = prompt("Family name", vc.credentialSubject!!.familyName)
        vc.credentialSubject!!.dateOfBirth = prompt("Date of birth", vc.credentialSubject!!.dateOfBirth)
        vc.credentialSubject!!.gender = prompt("Gender", vc.credentialSubject!!.gender)
        vc.credentialSubject!!.placeOfBirth = prompt("Place of birth", vc.credentialSubject!!.placeOfBirth)
        vc.credentialSubject!!.currentAddress = prompt("Current address", vc.credentialSubject!!.currentAddress)

        return vc
    }
}
