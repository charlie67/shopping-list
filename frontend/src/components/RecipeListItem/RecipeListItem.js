import {useState} from "react";
import {Button} from "react-bootstrap";

const RecipeListItem = (props) => {
  const [name] = useState(props.item.name);
  const [imageUrl] = useState(props.item.imageUrl);

  function addItems() {

  }

  return (
    <div className="flex flex-col justify-center items-center h-full">
      <a href={props.item.url} target="_blank" rel="noopener noreferrer"
         className="hover:pointer-events-auto text-gray-600">
        <h3>{name}</h3>
      </a>
      <img src={imageUrl} alt={name} className={"mt-auto my-auto"}/>
      <Button
        className={"account-button-connect bg-blue-500 hover:bg-blue-700 text-white font-bold mt-4 px-4 rounded-full"}
        onClick={() => addItems()}>Add Ingredients</Button>
    </div>
  );
};

export default RecipeListItem;