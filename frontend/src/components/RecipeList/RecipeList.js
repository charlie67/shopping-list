import React, {useState} from "react";
import {connect, useDispatch} from "react-redux";
import {fetchRecipes} from "../../actionTypes/RecipeActions";
import InfiniteScroll from "react-infinite-scroll-component";
import RecipeListItem from "../RecipeListItem/RecipeListItem";

const RecipeList = ({items, hasMore}) => {
  const dispatch = useDispatch();
  const [page, setPage] = useState(0);
  const [initialLoad, setInitialLoad] = useState(true);

  const loadMoreItems = () => {
    if (initialLoad) {
      setInitialLoad(false);
      // don't call this the first time because the first page is already loaded
      return;
    }

    const nextPage = page + 1;
    setPage(nextPage);
    dispatch(fetchRecipes(nextPage));
  };

  return (<div className={"list-scroll-container"}>
    <InfiniteScroll
      dataLength={items.length}
      next={loadMoreItems}
      hasMore={hasMore}
      loader={<h4 className={"loading-text"}>Loading...</h4>}
      scrollableTarget="shopping-list-container"
    >
      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-6 gap-4 p-4">
        {items.map((item, index) => (<div
          className="bg-white rounded-2xl shadow-md p-6 text-center"
          key={index}
        >
          <RecipeListItem item={item}/>
        </div>))}
      </div>
    </InfiniteScroll>
  </div>);
};

const mapStateToProps = (state) => {
  return {
    items: state.recipes.recipes, hasMore: state.recipes.hasMore
  };
};

export default connect(mapStateToProps)(RecipeList);