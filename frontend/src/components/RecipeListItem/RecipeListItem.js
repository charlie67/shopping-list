import React from 'react';
import PropTypes from 'prop-types';
import styles from './RecipeListItem.module.scss';

const RecipeListItem = () => (
  <div className={styles.RecipeListItem} data-testid="RecipeListItem">
    RecipeListItem Component
  </div>
);

RecipeListItem.propTypes = {};

RecipeListItem.defaultProps = {};

export default RecipeListItem;
