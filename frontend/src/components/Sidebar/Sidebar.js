import React from 'react';
import { push as Menu } from 'react-burger-menu';

import "./Sidebar.scss"
import {Link} from "react-router-dom";

class Sidebar extends React.Component {

  state = {
    isOpen: window.innerWidth > 1200
  }

  handleOnClose = (e) => {
    this.setState({isOpen: false})
  }

  render() {
    return (
      <Menu pageWrapId={"page-wrap"} outerContainerId={"outer-container"} onClose={ this.handleOnClose } disableCloseOnEsc noOverlay isOpen={this.state.isOpen}>
        <Link
          to={'/'} className="menu-item">
          Home
        </Link>
        <Link to={'/recipes'} className="menu-item">
          Recipes
        </Link>
      </Menu>
    );
  }
}

Sidebar.propTypes = {};

Sidebar.defaultProps = {};

export default Sidebar;
