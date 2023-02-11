import React from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheckSquare, faPlus, faSquare, faTrash} from "@fortawesome/free-solid-svg-icons";
import axios from 'axios';
import debounce from 'lodash.debounce';

import "./ShoppingList.scss"

class ShoppingList extends React.Component {

  state = {}

  constructor(props) {
    super(props);

    this.state = {
      items: [],
      hasMore: true,
      pageNumber: 0,
      input: ""
    }
  }

  async componentDidMount() {
    this.fetchMoreData()
  }

  // function to fetch the next page of data
  fetchMoreData = () => {
    console.log("fetching data")
    // generate the URL for the next page of data
    const url = `http://localhost:3001/api/todo/pageable/${this.state.pageNumber}`;

    // make a GET request to the URL to retrieve the data for the next page
    fetch(url)
      .then(response => response.json())
      .then(data => {
        console.log(data)
        // update the state with the new data
        this.setState({
          items: [...this.state.items, ...data.content],
          hasMore: !data.last,
          pageNumber: this.state.pageNumber + 1,
        });
      });
  }

  handleAddButtonClick = async (e) => {
    const targetDate = "123";
    const title = this.state.input;
    let response = ""

    try {
      response = await axios.post('http://localhost:3001/api/todo', {title, targetDate})

      this.setState({
        items: [response.data, ...this.state.items]
      });

    } catch(error){
      if (error.response) {
       console.log(error.response.data.message);
      } else {
        console.log('Error: something happened');
      }
      return;
    }

    this.setState({input: ""})
  }

  addItemKeyPress = async (e) => {
    if (e.key === 'Enter') {
      await this.handleAddButtonClick();
    }
  }


  deleteItem = async (id) => {
    try {
      await axios.delete(`http://localhost:3001/api/todo/${id}`);

      let i = 0;
      this.state.items.forEach((item) => {
        console.log(item)
        if (item.id === id) {
          let items = [...this.state.items];
          items.splice(i, 1);
          this.setState({items: items})
        }
        i++;
      })
    } catch (error) {
      if (error.response) {
        console.log(error.response.data.message);
      } else {
        console.log('Error: something happened');
      }
    }
  }

  markCompleted = async (id) => {
    console.log("marked completed ", id)
    try {
      await axios.put(`http://localhost:3001/api/todo/${id}/markcomplete`, {});

      let i = 0;
      this.state.items.forEach((item) => {
        console.log(item)
        if (item.id === id) {
          let items = [...this.state.items];
          let clonedItem = {...items[i]};
          clonedItem.completed = !clonedItem.completed;

          items[i] = clonedItem;

          this.setState({items: items});
        }
        i++;
      });

    } catch (error) {
      if (error.response) {
        console.log(error.response.data.message);
      } else {
        console.log('Error: something happened');
      }
      return;
    }
  }

  editShoppingListItem = async(id, title) => {
    console.log(id, title)

    try {
      await axios.put(`http://localhost:3001/api/todo/${id}`, {"title": title});

      let i = 0;
      this.state.items.forEach((item) => {
        console.log(item)
        if (item.id === id) {
          let items = [...this.state.items];
          let clonedItem = {...items[i]};
          clonedItem.title = title;

          items[i] = clonedItem;

          this.setState({items: items});
        }
        i++;
      });

    } catch (error) {
      console.log("Error in modifying item name", error)
    }
  }

  debouncedEditShoppingListItem = debounce(this.editShoppingListItem, 300);

  render() {
    return (
      <div className={"shopping-list-area"} >
        <div className="shopping-list-header">
          <h2 className="default-text">Shopping List Name todo</h2>
        </div>

        <div className='add-item-box list-item'>
          <FontAwesomeIcon className="add-item-icon icon" icon={faPlus} onClick={() => this.handleAddButtonClick()} />
          <input value={this.state.input} onChange={(event) => this.setState({input: event.target.value})} onKeyDown={this.addItemKeyPress} className='add-item-input' placeholder='Add an item...' />
        </div>

        <div className={"divider"}></div>

        <InfiniteScroll
          dataLength={this.state.items.length}
          next={this.fetchMoreData}
          hasMore={this.state.hasMore}
          loader={<h4>Loading...</h4>}
        >
          {this.state.items.map((item) => (
            <div className={'item-container list-item item-container-'+item.id} key={item.id}>
              {item.completed ? (
                <>
                  <FontAwesomeIcon className='complete-icon icon' icon={faCheckSquare} onClick={() => this.markCompleted(item.id)}/>
                  <span className='default-text item-name completed'>{item.title}</span>
                </>
              ) : (
                <>
                  <FontAwesomeIcon className='complete-icon icon' icon={faSquare} onClick={() => this.markCompleted(item.id)}/>
                  <span className='default-text item-name' contentEditable="true" onInput={e => this.debouncedEditShoppingListItem(item.id, e.currentTarget.textContent)}>{item.title}</span>
                </>
              )}
              <div className="delete-icon" onClick={() => this.deleteItem(item.id)}>
                <FontAwesomeIcon className="delete-icon icon" icon={faTrash}/>
              </div>
            </div>
          ))}
        </InfiniteScroll>
      </div>
    );
  }
}

ShoppingList.propTypes = {};

ShoppingList.defaultProps = {};

export default ShoppingList;
