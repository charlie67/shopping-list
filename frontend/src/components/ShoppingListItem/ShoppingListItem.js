import React, {useContext, useEffect, useState} from 'react';
import {WebSocketContext} from "../WebSocketProvider/WebSocketProvider";
import {faCheckSquare, faSquare, faTrash} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import axios from "axios";
import {SHOPPINGLIST_BASE} from "../../url_const";
import "./ShoppingListItem.scss";

const ShoppingListItem = (props) => {
    const [title, setTitle] = useState(props.item.title);
    const [completed, setCompleted] = useState(props.item.completed);
    const [id] = useState(props.item.id);
    const {lastJsonMessage} = useContext(WebSocketContext);

    const handleTitleChange = (event) => {
        setTitle(event.target.textContent);
    };

    useEffect(() => {
        if (lastJsonMessage && lastJsonMessage.messageType === "SHOPPING_LIST_ITEM_UPDATED" && lastJsonMessage.data.id === id) {
            setTitle(lastJsonMessage.data.title);
            setCompleted(lastJsonMessage.data.completed);
        }
    }, [lastJsonMessage, id]);

    const updateCompleteStatus = async (item) => {
        const id = item.id;
        const complete = !item.completed;
           axios.patch(SHOPPINGLIST_BASE + id, { "complete": complete }).catch(function (error) {
               console.error("error updating item", error);
           });
    };

    return (
        <div className="list-item">
            {completed ? (
                <>
                    <FontAwesomeIcon className='complete-icon icon' icon={faCheckSquare}
                                     onClick={() => updateCompleteStatus(props.item)}/>
                    <span className='default-text item-name completed'>{title}</span>
                </>
            ) : (
                <>
                    <FontAwesomeIcon className='complete-icon icon' icon={faSquare}
                                     onClick={() => updateCompleteStatus(props.item)}/>
                    <span
                        className='default-text item-name'
                        contentEditable={false}
                        onInput={handleTitleChange}
                    >
                {title}
                </span>
                </>
            )}
            <div className="delete-icon" onClick={() => props.deleteItem(id)}>
                <FontAwesomeIcon className="delete-icon icon" icon={faTrash}/>
            </div>
        </div>
    );
};

export default ShoppingListItem;