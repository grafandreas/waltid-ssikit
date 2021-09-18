package id.walt.services.keystore

import id.walt.crypto.*
import id.walt.services.hkvstore.HKVStoreService
import mu.KotlinLogging
import java.nio.file.Path

open class HKVKeyStoreService : KeyStoreService() {

    private val log = KotlinLogging.logger {}
    private val hkvStore = HKVStoreService.getService()

    //TODO: get key format from config
    private val KEY_FORMAT = KeyFormat.PEM

    override fun listKeys(): List<Key> = hkvStore.listChildKeys(Path.of("keys"))
        .map {
            load(it.fileName.toString().substringBefore("."))
        }

    override fun load(alias: String, keyType: KeyType): Key {
        log.debug { "Loading key \"${alias}\"." }

        //val keyId = getKeyId(alias) ?: alias
        val keyId = alias

        val metaData = loadKey(keyId, "meta").decodeToString()
        val algorithm = metaData.substringBefore(delimiter = ";")
        val provider = metaData.substringAfter(delimiter = ";")

        val publicPart = loadKey(keyId, "enc-pubkey").decodeToString()
        val privatePart = if (keyType == KeyType.PRIVATE) loadKey(keyId, "enc-privkey").decodeToString() else null


        return buildKey(keyId, algorithm, provider, publicPart, privatePart, KEY_FORMAT)
    }

    override fun addAlias(keyId: KeyId, alias: String) = TODO("Not implemented")

    override fun store(key: Key) {
        log.debug { "Storing key \"${key.keyId}\"." }
        //addAlias(key.keyId, key.keyId.id)
        storeKeyMetaData(key)
        storePublicKey(key)
        storePrivateKeyWhenExisting(key)
    }

    //override fun getKeyId(alias: String) = runCatching { File("${KEY_DIR_PATH}/Alias-$alias").readText() }.getOrNull()

    override fun delete(alias: String) {
        hkvStore.delete(Path.of("keys", alias), recursive = true)
    }

    private fun storePublicKey(key: Key) =
        saveKeyData(
            key, "enc-pubkey",
            when (KEY_FORMAT) {
                KeyFormat.PEM -> key.getPublicKey().toPEM()
                else -> key.getPublicKey().toBase64()
            }.toByteArray()
        )

    private fun storePrivateKeyWhenExisting(key: Key) {
        if (key.keyPair != null && key.keyPair!!.private != null) {
            saveKeyData(
                key, "enc-privkey", when (KEY_FORMAT) {
                    KeyFormat.PEM -> key.keyPair!!.private.toPEM()
                    else -> key.keyPair!!.private.toBase64()
                }.toByteArray()
            )
        }
    }

    private fun storeKeyMetaData(key: Key) {
        saveKeyData(key, "meta", (key.algorithm.name + ";" + key.cryptoProvider.name).toByteArray())
    }

    private fun saveKeyData(key: Key, suffix: String, data: ByteArray): Unit =
        hkvStore.put(Path.of("keys", key.keyId.id, suffix), data)

    private fun loadKey(keyId: String, suffix: String): ByteArray =
        hkvStore.getAsByteArray(Path.of("keys", keyId, suffix))
}
