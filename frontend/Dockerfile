FROM node:18-alpine3.19 AS build

WORKDIR /src

COPY package*.json ./

RUN ["npm", "ci"]

COPY ./ .

RUN ["npm", "run", "build"]

FROM nginx:1.27.2 AS prod

COPY ./nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /src/dist/frontend/browser/ /usr/share/nginx/html

CMD ["nginx", "-g", "daemon off;"]
