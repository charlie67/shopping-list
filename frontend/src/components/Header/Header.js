import React from 'react';

import "./Header.scss"
import {useNavigate} from "react-router-dom";

const Header = () => {
  const navigate = useNavigate();

  function navigateToPage(page) {
    navigate(page);
  }

  return (
    <header>
      <nav className="navbar text-white">
        <h3 className="default-text navbar-text" onClick={() => navigateToPage("/")}>Shopping List</h3>
        <h3 className="default-text navbar-text" onClick={() => navigateToPage("/recipes")}>Recipes</h3>
      </nav>
    </header>
  );
}
Header.propTypes = {};

Header.defaultProps = {};

export default Header;
