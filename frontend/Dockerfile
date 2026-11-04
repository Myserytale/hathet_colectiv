# Multi-stage Dockerfile for PulseVenue React (Vite) Frontend (Owner: DevOps - ISS-01)
FROM node:22-alpine AS builder
WORKDIR /app/frontend

COPY package.json ./
RUN npm install

COPY . ./
RUN npm run build

FROM nginx:1.27-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/frontend/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
