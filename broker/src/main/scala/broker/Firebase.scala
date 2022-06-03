package broker

import java.io.InputStream
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.database._

case class FirebaseException(s: String) extends Exception(s)

object Firebase {
  private val credentials : InputStream = getClass.getResourceAsStream("/firebaseCredentials.json")
  //  private val credentials : InputStream = getClass.getResourceAsStream("ptrlab-aeb3e-firebase-adminsdk-gyb13-ef2f2e67a9.json")
  private val options = new  FirebaseOptions.Builder()
    .setDatabaseUrl("https://broker-83359-default-rtdb.europe-west1.firebasedatabase.app/")
    .setServiceAccount(credentials)
    .build()
  FirebaseApp.initializeApp(options)
  private val database = FirebaseDatabase.getInstance()
  def ref(path: String): DatabaseReference = database.getReference(path)

}
