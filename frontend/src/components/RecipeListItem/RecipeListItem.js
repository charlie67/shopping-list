import {useState} from "react";

const RecipeListItem = (props) => {
  const [name, setName] = useState(props.item.name);
  const [imageUrl, setImageUrl] = useState(props.item.imageUrl);

  return (
    <div className="flex flex-col justify-center items-center h-full">
      <a href={props.item.url} target="_blank" rel="noopener noreferrer"
         className="hover:pointer-events-auto text-gray-600">
        <h3>{name}</h3>
      </a>
      <img src={imageUrl} alt={name} className={"mt-auto my-auto"}/>
    </div>
  );
};

export default RecipeListItem;