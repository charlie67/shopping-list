import React, {useState, useEffect, useContext} from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus } from "@fortawesome/free-solid-svg-icons";
import axios from 'axios';

import "./ShoppingList.scss";
import { SHOPPING_LIST_ADD_ITEM_ENDPOINT, SHOPPINGLIST_BASE } from "../../url_const";
import ShoppingListItem from "../ShoppingListItem/ShoppingListItem";
import {WebSocketContext} from "../WebSocketProvider/WebSocketProvider";
import {mapIncomingShoppingListItem} from "./util";
import {fetchShoppingList} from "../../actionTypes/actions";
import {connect} from "react-redux";
import { useDispatch } from 'react-redux';

const ShoppingList = ({ shoppingList, hasMore }) => {
  const [page, setPage] = useState(0);
  const [input, setInput] = useState("");
  const dispatch = useDispatch();

  const { lastJsonMessage } = useContext(WebSocketContext);

  useEffect(() => {
    console.log("here")
    dispatch(fetchShoppingList(0));
    console.log("shoppingList", shoppingList);
  }, []);

  const loadMoreShoppingListItems = () => {
    console.log("loadMoreShoppingListItems");
    const nextPage = page + 1;
    fetchShoppingList(nextPage); // Fetch shopping list items for the next page
    setPage(nextPage); // Update the page state variable
  };

  const handleAddButtonClick = async () => {
    const targetDate = "123";
    const title = input;

    axios.post(SHOPPING_LIST_ADD_ITEM_ENDPOINT, {title, targetDate}).then(function (response) {
      setInput("");
    }).catch(function (error) {
      console.error("error adding item", error);
    });
  }

  const addItemKeyPress = async (e) => {
    if (e.key === 'Enter') {
      await handleAddButtonClick();
    }
  }

  const deleteItem = async (id) => {
      await axios.delete(SHOPPINGLIST_BASE + id).catch(function (error) {
        console.error("error deleting item", error);
      });
  }

  const compareItems = (a, b) => {
    if (a.completed !== b.completed) {
      return a.completed ? 1 : -1;
    }

    if (a.completed) {
      return b.updatedAtTime - a.updatedAtTime;
    } else {
      return b.createdAtTime - a.createdAtTime;
    }
  }

  // const handleItemCompleteChange = (item) => {
  //   const id = item.id;
  //
  //   // let updatedItems = [...items];
  //   const index = updatedItems.findIndex(item => item.id === id);
  //   if (index !== -1) {
  //     updatedItems[index] = item;
  //   }
  //
  //   for (let i = 0; i < updatedItems.length; i++) {
  //     for (let j = i + 1; j < updatedItems.length; j++) {
  //       if (compareItems(updatedItems[j], updatedItems[i]) < 0) {
  //         [updatedItems[i], updatedItems[j]] = [updatedItems[j], updatedItems[i]];
  //       }
  //     }
  //   }
  //
  //   // setItems(updatedItems);
  // }

  useEffect(() => {
    if (lastJsonMessage && lastJsonMessage.messageType === "SHOPPING_LIST_ITEM_CREATED") {
      const item = mapIncomingShoppingListItem(lastJsonMessage.data);
      // setItems(prevItems => [item, ...prevItems]);
    } else if (lastJsonMessage && lastJsonMessage.messageType === "SHOPPING_LIST_ITEM_DELETED") {
      const id = lastJsonMessage.data.id;
      // setItems(prevItems => prevItems.filter(item => item.id !== id));
    } else if (lastJsonMessage && lastJsonMessage.messageType === "SHOPPING_LIST_ITEM_UPDATED") {
      const item = mapIncomingShoppingListItem(lastJsonMessage.data);
      // handleItemCompleteChange(item);
      // setItems(prevItems => prevItems.map(prevItem => prevItem.id === item.id ? item : prevItem));
    }
  }, [lastJsonMessage])

  return (
      <div className={"shopping-list-area"}>
        <div className="shopping-list-header">
          <h2 className="default-text">Shopping List Name todo</h2>
        </div>

        <div className='add-item-box list-item'>
          <FontAwesomeIcon className="add-item-icon icon" icon={faPlus} onClick={handleAddButtonClick} />
          <input value={input} onChange={(event) => setInput(event.target.value)} onKeyDown={addItemKeyPress} className='add-item-input' placeholder='Add an item...' />
        </div>

        <div className={"divider"}></div>

        <InfiniteScroll
            dataLength={shoppingList.length}
            next={loadMoreShoppingListItems}
            hasMore={hasMore}
            loader={<h4 className={"loading-text"}>Loading...</h4>}
        >
          {shoppingList.map((item) => (
              <div className={'item-container-' + item.id} key={item.id}>
                <ShoppingListItem item={item} deleteItem={deleteItem}/>
              </div>
          ))}
        </InfiniteScroll>
      </div>
  );
};

const mapStateToProps = (state) => ({
  shoppingList: state.shoppingList.shoppingListItems,
  hasMore: state.shoppingList.hasMore
});

export default connect(mapStateToProps)(ShoppingList);