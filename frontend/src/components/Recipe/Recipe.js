import React from 'react';
import InfiniteScroll from 'react-infinite-scroll-component';

import "./Recipe.scss"
import {faPlus, faBars, faGrip} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

class Recipe extends React.Component {

  state = {}

  constructor(props) {
    super(props);

    this.state = {
      pageNumber: 0,
      recipes: []
    }
  }

  async componentDidMount() {
    this.fetchMoreData()
  }

  // function to fetch the next page of data
  fetchMoreData = () => {
    const url = `http://localhost:3001/api/recipe/${this.state.pageNumber}`;

    fetch(url)
      .then(response => response.json())
      .then(data => {
        console.log(data)
        // update the state with the new data
        this.setState({
          recipes: [...this.state.recipes, ...data.content],
          hasMore: !data.last,
          pageNumber: this.state.pageNumber + 1,
        });
      });
  }

   async openModal() {
    setIsOpen(true);
  }

   async afterOpenModal() {
    // references are now sync'd and can be accessed.
    subtitle.style.color = '#f00';
  }

  async closeModal() {
    setIsOpen(false);
  }

  render() {
    return (
      <div className="recipe-area">
        <div className="recipe-header">
          <div className={"add-header-area"}>
            <FontAwesomeIcon className="add-item-icon icon" icon={faPlus} />
          </div>

          <div className={"layout-header-area"}>
            <FontAwesomeIcon className="view-icon icon" icon={faBars} />
            <FontAwesomeIcon className="view-icon icon" icon={faGrip} />
          </div>
        </div>

        {/*<Modal*/}
        {/*  isOpen={this.state.modalIsOpen}*/}
        {/*  onAfterOpen={this.afterOpenModal}*/}
        {/*  onRequestClose={this.closeModal}*/}
        {/*  contentLabel="Example Modal"*/}
        {/*>*/}
        {/*  <h2>Hello</h2>*/}
        {/*  <button onClick={this.closeModal}>close</button>*/}
        {/*  <div>I am a modal</div>*/}
        {/*  <form>*/}
        {/*    <input />*/}
        {/*    <button>tab navigation</button>*/}
        {/*    <button>stays</button>*/}
        {/*    <button>inside</button>*/}
        {/*    <button>the modal</button>*/}
        {/*  </form>*/}
        {/*</Modal>*/}

        <InfiniteScroll
          dataLength={this.state.recipes.length}
          next={this.fetchMoreData}
          hasMore={this.state.hasMore}
          loader={<h4>Loading...</h4>}
        >
          {this.state.recipes.forEach((recipe) => {
            <div> {recipe.title} </div>
          })}
        </InfiniteScroll>
      </div>
    )
  }
}

Recipe.propTypes = {};

Recipe.defaultProps = {};

export default Recipe;
