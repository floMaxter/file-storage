import {API_DIRECTORY} from "../../../../UrlConstants.jsx";
import {throwSpecifyException} from "../../../../exception/ThrowSpecifyException.jsx";


export const sendCreateFolder = async (path) => {

    console.log("Запрос на создание папки: " + path);


    const params = new URLSearchParams({ path: path});

    const url = `${API_DIRECTORY}?${params.toString()}`;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
    });

    console.log("Ответ на создание папки: ");
    console.log(response);
    if (!response.ok) {
        console.log("Ошибка со статусом: " + response.status);
        const errorMessage = await response.json();
        console.log(errorMessage);
        throwSpecifyException(response.status, errorMessage);
    }

    return await response.json();

}