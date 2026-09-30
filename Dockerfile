FROM node:22-alpine AS web
WORKDIR /web
COPY frontend/package*.json frontend/.npmrc ./
RUN npm ci
COPY frontend/ ./
ARG VERSION=dev
ENV VITE_APP_VERSION=$VERSION
RUN npm run build

FROM golang:1.27-alpine AS api
WORKDIR /src
ENV GOTOOLCHAIN=auto CGO_ENABLED=0
COPY backend/go.mod backend/go.sum ./
RUN go mod download
COPY backend/ ./
RUN go build -trimpath -ldflags="-s -w" -o /out/server ./cmd/server

FROM gcr.io/distroless/static-debian12:nonroot
WORKDIR /app
COPY --from=api /out/server /app/server
COPY --from=web /web/dist /app/web
ARG VERSION=dev
ENV APP_VERSION=$VERSION \
    STATIC_DIR=/app/web \
    UPLOAD_DIR=/tmp/uploads/avatars \
    LOGO_UPLOAD_DIR=/tmp/uploads/logos \
    PORT=8080
EXPOSE 8080
USER nonroot:nonroot
ENTRYPOINT ["/app/server"]
