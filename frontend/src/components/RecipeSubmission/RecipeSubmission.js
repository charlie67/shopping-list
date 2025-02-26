import {useState} from "react";
import axios from "axios";
import {EXTRACT_RECIPE_ENDPOINT} from "../../url_const";

export const RecipeSubmission = message => {
  const [newRecipeUrl, setNewRecipeUrl] = useState('');
  const [autoSave, setAutoSave] = useState(true);

  const handleChange = (e) => {
    setNewRecipeUrl(e.target.value);
  };

  const handleSubmit = () => {
    console.log("submitting", newRecipeUrl);


    axios.post(EXTRACT_RECIPE_ENDPOINT, null, {params: {url: newRecipeUrl, save: autoSave}})
         .then(response => response.status)
         .catch(error => {
           window.alert(`Error adding ${newRecipeUrl}`);
           console.error("error adding recipe", error);
         });

    setNewRecipeUrl('');
  };

  // const handleAutoSaveChange = (e) => {
  //   setAutoSave(e.target.checked);
  // };

  return (
    <div className="recipe-submission flex flex-col items-center p-4 rounded-md">
      <div className="flex items-center">
        <input
          type="text"
          value={newRecipeUrl}
          onChange={handleChange}
          placeholder={"Enter a URL to add"}
          className="w-64 p-2 border border-gray-300 rounded-l-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-gray-700 text-white"
        />
        <button
          onClick={handleSubmit}
          className="ml-2 p-2 bg-blue-500 text-white rounded-r-md hover:bg-blue-600"
        >
          Submit
        </button>
      </div>
      <div className="mt-1 flex items-center">
        <label className="mr-1 text-white">Auto save</label>
        <input
          type="checkbox"
          checked={autoSave}
          // onChange={handleAutoSaveChange}
          className="p-2 border border-gray-300 rounded-md bg-gray-700 text-white"
        />
        <span className="ml-2 text-white"
              title="When checked, the value will be saved directly to the DB. Otherwise, it will return the data that was extracted.">?</span>
      </div>
    </div>
  )
}