# Quiz Tebak Angka

**Nama project:** `Tugas-UTS_QuizTebakAngka_215314010`  
**Nama aplikasi:** Quiz Tebak Angka  
**Application ID:** `com.example.quiztebakangka`  
**Bahasa:** Kotlin  
**UI:** Android XML  
**Minimum Android:** Android 6.0 (API 23)  
**Compile SDK:** 35  
**JDK yang disarankan:** JDK 17 untuk Android Gradle Plugin 8.7.3

## Fitur
- Angka rahasia diacak pada rentang 0–100 setiap kali memulai permainan.
- Skor awal 100.
- Setiap tebakan yang salah mengurangi skor 10 poin, dengan batas bawah 0.
- Petunjuk apakah tebakan terlalu kecil atau terlalu besar.
- Permainan berakhir saat angka benar ditebak atau skor mencapai 0.
- Validasi input kosong dan angka di luar rentang 0–100.
- Menu utama, bantuan, informasi aplikasi, mulai ulang, dan kembali ke menu.
- UI responsif berbasis layout vertikal dan dapat digulir pada layar yang lebih kecil.
- Skor terbaik tersimpan permanen (SharedPreferences) dan tampil di menu.
- Rentang angka yang masih mungkin ditampilkan setelah tiap petunjuk.
- State permainan bertahan saat Activity dibuat ulang; tombol Back di game meminta konfirmasi.
- Semua teks berada di `strings.xml`; ikon launcher vektor.

## Perbaikan error build
Error `AAPT: error: resource attr/   Bantuan not found` di `activity_main.xml` disebabkan teks tombol
`android:text="?   Bantuan"`. Karakter `?` di awal nilai dibaca AAPT sebagai referensi atribut tema (`?attr/...`).
Solusi: seluruh teks dipindah ke `strings.xml` dan tombol kini bertuliskan `❓   Bantuan`.
Jangan memulai teks resource dengan `?` atau `@` tanpa escape (`\?`, `\@`).

## Membuka project
1. Ekstrak ZIP ke folder yang mudah ditemukan.
2. Buka Android Studio.
3. Pilih **Open**, lalu pilih folder `Tugas-UTS_QuizTebakAngka_215314010` (folder yang berisi `settings.gradle.kts`).
4. Gunakan JDK 17 melalui **Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JDK**.
5. Tunggu Gradle Sync selesai. Pastikan koneksi internet tersedia untuk mengunduh Android Gradle Plugin dan dependency build yang diperlukan.
6. Jalankan di emulator atau perangkat Android.

## Membuat APK untuk HP
1. Di Android Studio pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
2. Setelah build berhasil, tekan **Locate** pada notifikasi.
3. APK debug umumnya berada di `app/build/outputs/apk/debug/app-debug.apk`.
4. Pindahkan APK ke HP (USB, Drive, atau metode berbagi file), lalu buka file tersebut untuk memasang.
5. Jika diminta, izinkan instalasi aplikasi dari sumber tersebut pada pengaturan Android.

## Menguji di Samsung Galaxy A06
- Pastikan perangkat menggunakan Android yang didukung (Android 6.0/API 23 atau lebih baru).
- Aktifkan Developer options dan USB debugging bila ingin menjalankan langsung dari Android Studio.
- Hubungkan kabel USB, setujui dialog izin debugging di HP, lalu pilih perangkat di Android Studio dan tekan **Run**.
- Uji: mulai game, input kosong, angka -1/101, tebakan terlalu kecil/besar, tebakan benar, pengurangan skor, skor 0, restart, dan kembali ke menu.

## Catatan
Project disiapkan untuk dibuka dan dibangun di Android Studio. Gradle Wrapper dibuat otomatis oleh Android Studio saat sync. Project belum dibangun ataupun diuji pada perangkat fisik Samsung Galaxy A06 di sesi ini. Hasil build bergantung pada versi Android Studio/SDK yang terpasang dan keberhasilan Gradle mengunduh plugin. Jika Android Studio meminta instalasi Android SDK Platform 35, pasang melalui SDK Manager.
