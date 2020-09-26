package com.pesan.todoappbasic

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

//buat vairable database reference yang akan diisi oleh database firebase
private lateinit var databaseRef: DatabaseReference

    //variable cekData dibuat untuk read
    private lateinit var cekData: DatabaseReference

    //untuk memantau perubahan database
    private lateinit var readDataListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseRef = FirebaseDatabase.getInstance().reference
        //ketika tombol tambah diklik
        btn_tambah.setOnClickListener {
            //ambil text dari edittext input nama
            val nama = input_nama.text.toString()
            if (nama.isBlank()){
                toastData("Kolom Nama Harus Diisi")
            } else {
                tambahData(nama)
            }
        }

        btn_hapus.setOnClickListener {
            val nama = input_nama.text.toString()
            if (nama.isBlank()){
                toastData("Kolom Kalimat Harus Diisi")
            } else {
                hapusData(nama)
            }

        }

        btn_update.setOnClickListener {
            val kalimatAwal = input_nama.text.toString()
            val kalimatUpdate = edit_nama.text.toString()
            if (kalimatAwal.isBlank() || kalimatUpdate.isBlank()){
                toastData("Kolom tidak boleh kosong")
            } else {
                updateData(kalimatAwal, kalimatUpdate)
            }

        }

        //untuk get data dari database firebase
        cekDataKalimat()
    }

    private fun updateData(kalimatAwal: String, kalimatUpdate: String) {
        val dataUpdate = HashMap<String, Any>()
        dataUpdate["Nama"] = kalimatUpdate

        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.childrenCount > 0){
                    databaseRef.child("Daftar Nama")
                        .child(kalimatAwal)
                        .updateChildren(dataUpdate)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("Data Berhasil Diupdate")
                        }
                } else {
                    toastData("Data Yang dituju tidak ada di dalam database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }

        val dataAsal = databaseRef.child("Daftar Nama")
            .child(kalimatAwal)
        dataAsal.addListenerForSingleValueEvent(dataListener)
    }

    private fun hapusData(nama: String) {
        //membuat listener data firebase
        val dataListener = object : ValueEventListener{
            //onDataChange itu untuk mengetahui aktifitas data
            //seperti penambahan, pengurangan, dan perubahan data
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot.childrenCount untuk mengetahui banyak data yg telah di ambil
                if (snapshot.childrenCount > 0){
                    //jika data tersebut ada, maka hapus field nama yang ada di dalam tabel Daftar Nama
                    databaseRef.child("Daftar Nama").child(nama)
                        .removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) toastData("$nama telah dihapus")
                        }
                } else {
                    toastData("Tidak ada data $nama")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toastData("tidak bisa menghapus data tersebut")
            }
        }
        //untuk menghapus data, kita perlu cek data yang ada di dalam tabel Daftar Nama
        val cekData = databaseRef.child("Daftar Nama")
            .child(nama)
        // addValueEventListener itu menjalankan Listener terus menerus selama data yang diinputkan sama
        // sedangkan addListenerForSingleValueEvent itu dijalankan sekali saja
        cekData.addListenerForSingleValueEvent(dataListener)
    }

    private fun cekDataKalimat() {
        readDataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //cek apakah data ada di dalam database
                if (snapshot.childrenCount > 0){
                    var textData = ""
                    for (data in snapshot.children){
                        val nilai = data.getValue(ModelName::class.java) as ModelName
                        textData += "${nilai.Nama} \n"
                    }

                    txt_nama.text = textData
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        }
        // cekdata menuju ke database firebase di tabel "Daftar Nama"
        cekData = databaseRef.child("Daftar Nama")
        //addValueEventListener digunakan untuk memantau perubahan database di tabel Daftar Nama
        cekData.addValueEventListener(readDataListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        cekData.removeEventListener(readDataListener)
    }

    private fun tambahData(nama: String) {
        val data = HashMap<String, Any>()
        data["Nama"] = nama

        //logika penambahan data, yaitu cek terlebih dahulu data
        //kemudian tambahkan data jika data belum ada
        val dataListener = object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                //snapshot childrecount ini menghitung jumlah data
                //jika data kurang dari satu maka pasti tidak ada data jadi tambahkan data
                if (snapshot.childrenCount < 1){
                    val tambahData = databaseRef.child("Daftar Nama")
                        .child(nama)
                        .setValue(data)
                    tambahData.addOnCompleteListener { task ->
                        if (task.isSuccessful){
                            toastData("$nama telah ditambahkan dalam database")
                        } else {
                            toastData("$nama gagal ditambahkan")
                        }
                    }
                } else {
                    toastData("Data tersebut sudah ada di database")
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                toastData("Terjadi error saat menambah data")
            }
        }

        //untuk mengecek tabel daftar nama apakah data yang ingin diinputkan ke tabel tersebut sudah ada
        databaseRef.child("Daftar Nama")
            .child(nama).addListenerForSingleValueEvent(dataListener)
    }

    private fun toastData(pesan: String){
        Toast.makeText(this, pesan, Toast.LENGTH_SHORT).show()
    }

}