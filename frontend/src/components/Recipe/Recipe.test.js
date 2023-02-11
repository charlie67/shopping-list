import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import Recipe from './Recipe';

describe('<Recipe />', () => {
  test('it should mount', () => {
    render(<Recipe />);
    
    const recipe = screen.getByTestId('Recipe');

    expect(recipe).toBeInTheDocument();
  });
});