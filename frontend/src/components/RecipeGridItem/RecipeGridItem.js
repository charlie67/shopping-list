import React from 'react';
import PropTypes from 'prop-types';
import styles from './RecipeGridItem.module.scss';

const RecipeGridItem = () => (
  <div className={styles.RecipeGridItem} data-testid="RecipeGridItem">
    RecipeGridItem Component
  </div>
);

RecipeGridItem.propTypes = {};

RecipeGridItem.defaultProps = {};

export default RecipeGridItem;
