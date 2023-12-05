import WebSocket from "ws";
import {jwtConfig} from "./utils.js";
import jwt from "jsonwebtoken";


let wss;

export const initWss = value => {
    wss = value;
    wss.on('connection', ws => {
        console.log("idk");
        ws.on('message', message => {
            const {type, payload: {token}} = JSON.parse(message);
            if(type !== 'authorization')
            {
                ws.close();
                return;
            }
            try{
                console.log(token);
                ws.user = jwt.verify(token, jwtConfig.secret);
            } catch (err) {
                ws.close();
            }
        })
    });
};

export const broadcast = (userId, data) => {
    if(!wss){
        return;
    }
    wss.clients.forEach(client => {
        console.log("Attempt to broadcast to client");
        if(client.readyState === WebSocket.OPEN && userId === client.user._id) {

            console.log(`broadcast sent to ${client.user.username}`);
            client.send(JSON.stringify(data));
        }
    });
};