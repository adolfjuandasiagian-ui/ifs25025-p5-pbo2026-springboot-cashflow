1. Salin LoginView.java dan RegisterView.java ke:
   src/main/java/org/delcom/ifs25025springbootcashflow/framework/view/
   (timpa file lama), lalu: git add . && git commit -m "Perbaiki login untuk pengujian" && git push

2. Render akan deploy ulang otomatis. Tunggu Live, buka aplikasi, lalu uji ulang di Delcom.

3. Jika login MANUAL di browser juga gagal dengan username/password yang sama,
   berarti masalahnya kredensial/database, bukan kode. Reset password admin di database
   (contoh password baru: admin) dengan SQL berikut:

   UPDATE users SET password_hash = '8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918' WHERE username = 'admin';

   Lalu isi Delcom dengan username: admin, password: admin.