import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import RecipeListItem from './RecipeListItem';

describe('<RecipeListItem />', () => {
  test('it should mount', () => {
    render(<RecipeListItem />);
    
    const recipeListItem = screen.getByTestId('RecipeListItem');

    expect(recipeListItem).toBeInTheDocument();
  });
});