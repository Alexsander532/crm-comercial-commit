# ================================================================
# STAGE 1: BUILD
# ================================================================
FROM node:20-alpine AS build

WORKDIR /app

# Copiar dependências primeiro (cache de layers)
COPY package.json package-lock.json ./
RUN npm ci

# Copiar código fonte e compilar
COPY . .
RUN npm run build

# ================================================================
# STAGE 2: SERVE (Nginx)
# ================================================================
FROM nginx:1.25-alpine

# Copiar arquivos buildados
COPY --from=build /app/dist /usr/share/nginx/html

# Configurar Nginx para SPA (fallback para index.html)
RUN echo 'server { \
    listen 80; \
    root /usr/share/nginx/html; \
    index index.html; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
    location /api { \
        proxy_pass http://backend:8080; \
        proxy_set_header Host $host; \
        proxy_set_header X-Real-IP $remote_addr; \
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; \
        proxy_set_header X-Forwarded-Proto $scheme; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
