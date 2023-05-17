import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import ShoppingList from './ShoppingList';

describe('<ShoppingList />', () => {
  test('it should mount', () => {
    render(<ShoppingList />);
    
    const shoppingList = screen.getByTestId('ShoppingList');

    expect(shoppingList).toBeInTheDocument();
  });
});