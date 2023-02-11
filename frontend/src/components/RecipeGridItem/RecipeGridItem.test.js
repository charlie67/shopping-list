import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import RecipeGridItem from './RecipeGridItem';

describe('<RecipeGridItem />', () => {
  test('it should mount', () => {
    render(<RecipeGridItem />);
    
    const recipeGridItem = screen.getByTestId('RecipeGridItem');

    expect(recipeGridItem).toBeInTheDocument();
  });
});