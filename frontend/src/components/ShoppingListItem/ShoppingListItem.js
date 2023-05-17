import React, { Component } from 'react';

class ShoppingListItem extends Component {
    constructor(props) {
        super(props);
        this.state = {
            title: props.item.title
        };
        this.handleTitleChange = this.handleTitleChange.bind(this);
    }

    handleTitleChange(event) {
        const newTitle = event.target.textContent;
        this.setState({ title: newTitle });
        this.props.debouncedEditShoppingListItem(this.props.item.id, newTitle);
    }

    render() {
        return (
            <span
                className='default-text item-name'
                contentEditable={false}
                onInput={this.handleTitleChange}
            >
        {this.state.title}
      </span>
        );
    }
}

export default ShoppingListItem;