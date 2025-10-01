# Build stage
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files and install dependencies
COPY frontend/package*.json ./
RUN npm install

# Install Angular CLI globally
RUN npm install -g @angular/cli@17

# Copy source code
COPY frontend/ .

# Build the application for production
RUN ng build --configuration production

# Production stage
FROM nginx:alpine

# Copy nginx configuration
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Copy built application
COPY --from=build /app/dist/echo-dashboard /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
