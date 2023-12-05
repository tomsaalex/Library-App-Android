import Router from 'koa-router'
import dataStore from "nedb-promise";
import { broadcast } from './wss.js'

export class BookStore {
    constructor({ filename, autoload }) {
        this.store = dataStore({ filename, autoload });
    }

    async find(props){
        return this.store.find(props);
    }

    async findOne(props) {
        return this.store.findOne(props);
    }

    async insert(book) {

        if (!book.title || !book.publicationDate || !book.pageCount || book.hasHardcover === undefined) { // validation
            let errorText = "Missing fields: \n";
            if(!book.title) errorText += " - Book title\n";
            if(!book.pageCount) errorText += " - Book page count\n";
            if(!book.publicationDate) errorText += " - Book publication date\n";
            if(!book.hasHardcover) errorText += " - Book hardcover status\n";

            throw new Error(errorText);
        }

        return this.store.insert(book);
    }

    async update(props, book) {
        return this.store.update(props, book);
    }

    async remove(props) {
        return this.store.remove(props);
    }
}

const bookStore = new BookStore({ filename: './db/books.json', autoload: true });

export const bookRouter = new Router();

bookRouter.get('/', async (ctx) => {
    const userId = ctx.state.user._id;
    ctx.response.body = await bookStore.find({ userId });
    ctx.response.status = 200;
});

bookRouter.get('/id', async (ctx) => {
    const userId = ctx.state.user._id;
    const book = await bookStore.findOne({ _id: ctx.params.id });

    if(book) {
        if(book.userId === userId)
        {
            ctx.response.body = book;
            ctx.response.status = 200; // ok
        }
        else
        {
            ctx.response.status = 403; // forbidden
        }
    } else {
        ctx.response.status = 404; // not found
    }
});

const createBook = async (ctx, book, response) => {
    try{
        const userId = ctx.state.user._id;
        book.userId = userId;
        if(book._id.length === 0) delete book._id;

        // book.publicationDate = new Date(book.publicationDate);
        response.body = await bookStore.insert(book);
        book = response.body;
        console.log(response.body);
        response.status = 201; // created
        console.log("broadcast done");
        console.log(book);
        broadcast(userId, { type: 'created', payload: book });
    } catch (err) {
        response.body = { message: err.message };
        response.status = 400; // bad request
    }
}

bookRouter.post('/', async ctx => await createBook(ctx, ctx.request.body, ctx.response));

bookRouter.put('/:id', async ctx => {
    console.log("Putting");
    const id = ctx.params.id;
    const book = ctx.request.body;
    const bookId = book._id;
    const response = ctx.response;
    if(bookId && bookId !== id)
    {
        ctx.response.body = { issue: [{ error: `Param id and body id should be the same` }] };
        ctx.response.status = 400; // BAD REQUEST
        return;
    }
    if(!bookId){
        await createBook(ctx, book, response);
    } else{
        const userId = ctx.state.user._id;
        book.userId = userId;
        console.log("Book is still going", book);
        const updatedCount = await bookStore.update({ _id: id }, book, {});
        console.log(updatedCount);
        if(updatedCount === 1)
        {
            response.body = book;
            response.status = 200; // ok
            broadcast(userId, { type: 'updated', payload: book });
        } else {
            response.body = { message: 'Resource no longer exists' };
            response.status = 405; // method not allowed
        }

    }
});

bookRouter.del('/:id', async (ctx) => {
    const userId = ctx.state.user._id;
    const book = await bookStore.findOne({ _id: ctx.params.id });

    if(book && userId !== book.userId) {
        ctx.response.status = 403; // forbidden
    } else {
        await bookStore.remove({ _id: ctx.params.id });
        ctx.response.status = 204; // no content
        //TODO: potential broadcast missing?
        broadcast({ event: 'deleted', payload: { book } });
    }
});