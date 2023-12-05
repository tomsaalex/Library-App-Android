export const jwtConfig = { secret: 'no-one-will-ever-know-this' }

export const exceptionHandler = async (ctx, next) => {
    try{
        return await next();
    } catch (err)
    {
        console.log(err);
        ctx.body = { message: err.message || 'Unexpected error.' };
        ctx.status = err.status || 500;
    }
}

export const timingLogger = async(ctx, next) => {
    const start = Date.now();
    await next();
    console.log(`${ctx.method} ${ctx.url} => ${ctx.response.status}, ${Date.now() - start}ms`);
}