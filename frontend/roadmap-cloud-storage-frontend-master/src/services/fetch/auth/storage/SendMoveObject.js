import {API_MOVE_FILES} from "../../../../UrlConstants.jsx";
import {throwSpecifyException} from "../../../../exception/ThrowSpecifyException.jsx";


export const sendMoveObject = async (source, target) => {

    console.log("Перемещение:");
    console.log( source + ' --> ' + target);

    const params = new URLSearchParams({from: source, to: target});

    const url = `${API_MOVE_FILES}?${params.toString()}`;

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    });

    console.log("Ответ на запрос о перемещении: ");
    console.log(response);
    if (!response.ok) {
        console.log("Ошибка со статусом: " + response.status);
        const error = await response.json();
        throwSpecifyException(response.status, error);
    }

    return await response.json();

}