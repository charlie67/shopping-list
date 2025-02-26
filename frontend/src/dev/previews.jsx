import React from 'react'
import {ComponentPreview, Previews} from '@react-buddy/ide-toolbox'
import {PaletteTree} from './palette'
import Header from "../components/Header/Header";
import ShoppingList from "../components/ShoppingList/ShoppingList";
import App from "../App";
import {RecipeList} from "../components/RecipeList/RecipeList";

const ComponentPreviews = () => {
  return (
      <Previews palette={<PaletteTree/>}>
        <ComponentPreview path="/Header">
          <Header/>
        </ComponentPreview>
        <ComponentPreview
            path="/ShoppingList">
          <ShoppingList/>
        </ComponentPreview>
          <ComponentPreview path="/App">
              <App/>
          </ComponentPreview>
        <ComponentPreview path="/RecipeList">
          <RecipeList/>
        </ComponentPreview>
      </Previews>
  )
}

export default ComponentPreviews