import http from 'http';
import Koa from 'koa';
import WebSocket from 'ws';
import Router from 'koa-router';
import bodyParser from "koa-bodyparser";
import jwt from 'koa-jwt';
import cors from '@koa/cors';
import { jwtConfig, timingLogger, exceptionHandler } from './utils.js';
import { initWss } from './wss.js';
import { bookRouter } from './book.js';
import { authRouter } from './auth.js';



const app = new Koa();
const server = http.createServer(app.callback());
const wss = new WebSocket.Server({ server });

initWss(wss);

app.use(cors());
app.use(timingLogger);
app.use(exceptionHandler);
app.use(bodyParser());

const prefix = '/api';

// public
const publicApiRouter = new Router({ prefix });
publicApiRouter.use('/auth', authRouter.routes());
app.use(publicApiRouter.routes())
    .use(publicApiRouter.allowedMethods());

app.use(jwt(jwtConfig));

//protected
const protectedApiRouter = new Router({ prefix });
protectedApiRouter.use('/book', bookRouter.routes());

app.use(protectedApiRouter.routes())
   .use(protectedApiRouter.allowedMethods());


server.listen(3000);
console.log("started on port 3000");

//setInterval(() => {
//  lastUpdated = new Date();
//  lastId = `${parseInt(lastId) + 1}`;
//  const book = new Book({ id: lastId, title: `book ${lastId}`, publicationDate: lastUpdated, pageCount: 430, hasHardcover: true });
//  books.push(book);
//  broadcast({ event: 'created', payload: { book } });
//}, 15000);

// app.use(router.routes());
// app.use(router.allowedMethods());
