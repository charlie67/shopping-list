import {useEffect} from "react";
import {useDispatch} from "react-redux";
import {fetchShoppingList} from "../../actionTypes/actions";

export const RecipeList = () => {
	const dispatch = useDispatch();

	useEffect(() => {
		dispatch(fetchShoppingList(0));
	}, [dispatch]);

	return (
		<></>
	);
};