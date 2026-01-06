# 🌴 SawIt — Smart Palm Plantation Management App

**SawIt** adalah aplikasi Android Native cerdas yang dirancang untuk modernisasi manajemen perkebunan kelapa sawit. Dengan mengintegrasikan **Machine Learning (ML)**, SawIt membantu petani memantau kondisi kebun, mencatat aktivitas secara terstruktur, dan memprediksi hasil panen dengan akurasi tinggi.

---

## 🌾 Latar Belakang & Tantangan
Sebagai produsen kelapa sawit terbesar di dunia, Indonesia membutuhkan digitalisasi di sektor agrikultur. SawIt hadir untuk menjawab tantangan utama:
- **Data Tidak Terstruktur:** Mengganti pencatatan manual tradisional dengan database yang aman.
- **Ketidakpastian Hasil:** Memberikan estimasi panen berdasarkan data historis cuaca dan kondisi lahan.

---

## ✨ Fitur Utama

### 1. 🗺️ Manajemen Lahan (Field Management)
- **Pemetaan Interaktif:** Integrasi Google Maps SDK untuk menentukan titik koordinat kebun secara presisi.
- **Snapshot Otomatis:** Sistem secara otomatis mengambil gambar peta sebagai *placeholder* visual lahan jika pengguna tidak mengunggah foto profil kebun.
- **Optimasi Memori:** Implementasi penyimpanan foto berbasis *Local File Path* untuk mencegah error `TransactionTooLargeException`.

### 2. 🌦️ Integrasi Cuaca Real-time
- Pengambilan data cuaca otomatis berdasarkan lokasi GPS terkini menggunakan **OpenWeatherMap API**.
- **Smart Permission Handling:** Sistem permintaan izin lokasi yang intuitif dengan *Rationale Dialog* untuk mengedukasi pengguna.

### 3. 🤖 Prediksi Berbasis Machine Learning
- **Prediction Yield:** Menghitung estimasi total panen (kg) berdasarkan variabel curah hujan, suhu, dan luas lahan.
- **Prediction Condition:** Menganalisis kondisi lahan sawit berdasarkan hasil panen.
- **Synchronized Validation:** Validasi pintar yang mencegah input yang tidak valid.

### 4. 📅 Timeline & Aktivitas
- Pelacakan aktivitas harian (Pemupukan, Panen, Perawatan) dalam bentuk *Activity Timeline* yang informatif.
- Sistem notifikasi untuk pengingat jadwal aktivitas yang sudah direncanakan.

---

## 🏗️ Arsitektur & Teknologi

Aplikasi ini dibangun menggunakan arsitektur **MVVM (Model-View-ViewModel)** untuk memastikan kode yang bersih (*Clean Code*), mudah diuji, dan skalabel.

| Komponen | Teknologi |
|-----------|------------|
| **Bahasa** | Kotlin |
| **UI Framework** | XML (View Binding) & Jetpack Compose (Hybrid) |
| **Database** | Firebase (Real-time Cloud Database) |
| **Authentication** | Firebase (Authenticator) |
| **Location Services** | Google Maps SDK & Fused Location Provider |
| **Image Loading** | Glide (dengan Disk Caching) |
| **API** | Retrofit & OpenWeatherMap API |

---

## 🛠️ Detail Teknis & Optimasi
Dalam pengembangan terbaru, kami berfokus pada stabilitas aplikasi:
- **Memory Management:** Menghapus penggunaan *Base64 String* pada model data untuk menjaga stabilitas transaksi antar fragment.
- **UI State Persistence:** Sinkronisasi *Bottom Bar* kustom dan Toolbar *Header* saat navigasi mendalam (Field → Logs → Prediction).
- **Safe Data Flow:** Penggunaan `StateFlow` dan `SharedViewModel` untuk sinkronisasi data antar fragment secara real-time.

---

## 👥 Anggota Kelompok

1. **Maureen Alexandria** (00000107632)
2. **Steven Lee** (00000105886)
3. **Dewangga Vito Smaradhana** (00000107630)
4. **Sandy Bonfilio Yuvens** (00000106442)

---

## 🚀 Tautan Proyek

- **💻 GitHub Repository:** [sandyby/uts-map](https://github.com/sandyby/uts-map)
- **📽️ Demo Video:** [Google Drive](https://drive.google.com/file/d/1moYgpT3f-gIHQQXMgb7aXMbdWd6IsuWN/view?usp=drive_link)
- **Google Drive:** [Google Drive](https://drive.google.com/drive/folders/1BEDI1ySwo5we8OBokl64_J-g6ID1t-1C?usp=drive_link)
---

**SawIt** — *Langkah nyata menuju transformasi digital agrikultur Indonesia.*
