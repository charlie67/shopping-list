import {RecipeSubmission} from "../components/RecipeSubmission/RecipeSubmission";
import RecipeList from "../components/RecipeList/RecipeList";

const RecipePage = () => {

  return (
    <div className={"recipe-page-container"}>
      <div className={"recipe-submission-container"}>
        <RecipeSubmission/>
      </div>

      <div className={"recipe-list-container"}>
        <RecipeList/>
      </div>
    </div>
  );
};

export default RecipePage;
