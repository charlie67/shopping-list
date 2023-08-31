import React, {useState, useEffect} from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faPlus } from "@fortawesome/free-solid-svg-icons";
import axios from 'axios';

import "./ShoppingList.scss";
import { SHOPPING_LIST_ADD_ITEM_ENDPOINT, SHOPPINGLIST_BASE } from "../../url_const";
import ShoppingListItem from "../ShoppingListItem/ShoppingListItem";
import {fetchShoppingList, shoppingListItemCreated} from "../../actionTypes/actions";
import {connect} from "react-redux";
import { useDispatch } from 'react-redux';

const ShoppingList = ({ shoppingList, hasMore }) => {
  const [page, setPage] = useState(0);
  const [input, setInput] = useState("");
  const dispatch = useDispatch();

  useEffect(() => {
    dispatch(fetchShoppingList(0));
  }, [dispatch]);

  const loadMoreShoppingListItems = () => {
    const nextPage = page + 1;
    setPage(nextPage);
    dispatch(fetchShoppingList(nextPage));
  };



  const handleAddButtonClick = async () => {
    const targetDate = "123";
    const title = input;

    axios.post(SHOPPING_LIST_ADD_ITEM_ENDPOINT, {title, targetDate}).then(function (response) {
      dispatch(shoppingListItemCreated(response.data));
      setInput("");
    }).catch(function (error) {
      console.error("error adding item", error);
      window.alert("Error adding item", error);
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

  return (
      <div className={"shopping-list-area"}>

        <div className='add-item-box list-item'>
          <FontAwesomeIcon className="add-item-icon icon" icon={faPlus} onClick={handleAddButtonClick} />
          <input value={input} onChange={(event) => setInput(event.target.value)} onKeyDown={addItemKeyPress} className='add-item-input' placeholder='Add an item...' />
        </div>

        <div className={"divider"}></div>

        <div className={"list-scroll-container"}>
          <InfiniteScroll
              dataLength={shoppingList.length}
              next={loadMoreShoppingListItems}
              hasMore={hasMore}
              loader={<h4 className={"loading-text"}>Loading...</h4>}
              scrollableTarget="shopping-list-container"
          >
            {shoppingList.map((item) => (
                <div className={'item-container item-container-' + item.id} key={item.id}>
                  <ShoppingListItem item={item} deleteItem={deleteItem}/>
                </div>
            ))}
          </InfiniteScroll>
        </div>
      </div>
  );
};

const mapStateToProps = (state) => ({
  shoppingList: state.shoppingList.shoppingListItems,
  hasMore: state.shoppingList.hasMore
});

export default connect(mapStateToProps)(ShoppingList);