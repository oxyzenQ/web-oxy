# Gunakan image yang telah ada sebagai dasar (base image)
FROM nginx:alpine

# Salin file-file proyek ke dalam direktori root NGINX di dalam kontainer
COPY base.html /usr/share/nginx/html/
COPY styles.css /usr/share/nginx/html/

# Port yang akan di-expose oleh container
EXPOSE 80

# Perintah untuk menjalankan NGINX dalam container ketika container dimulai
CMD ["nginx", "-g", "daemon off;"]
