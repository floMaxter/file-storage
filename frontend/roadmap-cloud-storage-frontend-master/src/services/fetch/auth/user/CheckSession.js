import {API_USER_INFO} from "../../../../UrlConstants.jsx";
import UnauthorizedException from "../../../../exception/UnauthorizedException.jsx";


export const checkSession = async () => {

    const response = await fetch(API_USER_INFO, {
        method: 'GET',
        credentials: 'include'
    });

    console.log("Проверка сессии: ");
    console.log(response);
    if (!response.ok) {
        console.log("Ошибка со статусом: " + response.status);
        const error = await response.json();
        throw new UnauthorizedException(error.detail);
    }

    return await response.json();
}