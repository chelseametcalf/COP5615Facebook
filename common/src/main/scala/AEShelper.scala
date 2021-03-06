import javax.crypto._
import javax.crypto.spec.{SecretKeySpec, GCMParameterSpec}
import java.security.{PrivateKey, SecureRandom}
import java.util.{Base64}

class AEShelper {

  val AES_KEY_SIZE = 128
  val GCM_NONCE_LENGTH = 12
  val GCM_TAG_LENGTH = 16

  val cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE")
  val random = SecureRandom.getInstanceStrong()

  def generateKey() : (SecretKey, Array[Byte]) = {
    val keyGen = KeyGenerator.getInstance("AES")
    val nonce = new Array[Byte](GCM_NONCE_LENGTH)
    keyGen.init(AES_KEY_SIZE, random)
    val key = keyGen.generateKey()
    random.nextBytes(nonce)
    (key, nonce)
  }

  def AESencrypt(key : SecretKey, nonce : Array[Byte], input : String) : Array[Byte] = {
    assert(nonce.length == GCM_NONCE_LENGTH)

    val spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
    cipher.init(Cipher.ENCRYPT_MODE, key, spec)
    val cipherText = cipher.doFinal(input.getBytes())
    //println("CIPHER TEXT: " + cipherText)
    cipherText
  }

  def AESdecrypt(key : SecretKey, nonce : Array[Byte], cipherText : Array[Byte]) : String = {
    assert(nonce.length == GCM_NONCE_LENGTH)

    val spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce)
    cipher.init(Cipher.DECRYPT_MODE, key, spec)
    val plainText = cipher.doFinal(cipherText)
    //println("PLAIN TEXT: " + plainText)
    new String(plainText)
  }

  // allows key to be passed in
  def encryptMessage(key : SecretKey, nonce : Array[Byte],
                     message : String, public_key : String) : (String, String, String) = {
    val e = AESencrypt(key, nonce, message)
    val encryptedMsg = Base64.getEncoder.encodeToString(e)

    val strAESkey = Base64.getEncoder.encodeToString(key.getEncoded)

    val str_RSA_AES_KEY = encryptKey(key, public_key)
    //val rsa = new RSAhelper()
    //val RSA_AES_KEY = rsa.encrypt(strAESkey, rsa.getPublicKey(public_key))
    //val str_RSA_AES_KEY = Base64.getEncoder.encodeToString(RSA_AES_KEY)

    val str_AESnonce = Base64.getEncoder.encodeToString(nonce)

    //println("Nonce: " + str_AESnonce)
    //println("Msg: " + encryptedMsg)
    //println("Key: " + key.getEncoded)

    (encryptedMsg, str_RSA_AES_KEY, str_AESnonce)
  }

  def encryptKey(key : SecretKey, public_key : String) : String = {
    val rsa = new RSAhelper()
    val encKey = rsa.encrypt(Base64.getEncoder.encodeToString(key.getEncoded), rsa.getPublicKey(public_key))
    Base64.getEncoder.encodeToString(encKey)
  }

  def encryptMessage(message : String, public_key : String) : (String, String, String) = {
    val aesKeyMaterial = generateKey()
    val key = aesKeyMaterial._1
    val nonce = aesKeyMaterial._2

    encryptMessage(key, nonce, message, public_key)
  }

  def decryptMessage(message : String, private_key : PrivateKey, rsa_pub_key : String, nonce : String) : String = {
    val byteNonce = Base64.getDecoder.decode(nonce)
    val byteMessage = Base64.getDecoder.decode(message)
    val rsa = new RSAhelper()

    val byte_rsa_pub = Base64.getDecoder.decode(rsa_pub_key)

    val str_aes_key = rsa.decrypt(byte_rsa_pub, private_key)
    val aes_key = getSecretKey(str_aes_key)
    val str_msg = AESdecrypt(aes_key, byteNonce, byteMessage)
    str_msg
  }

  def getSecretKey(string_key : String) : SecretKey = {
    val encodedKey = Base64.getDecoder.decode(string_key)
    //val keyBytes = string_key.getBytes()
    val originalKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES")
    originalKey
  }
}