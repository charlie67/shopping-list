import ShoppingList from "../components/ShoppingList/ShoppingList";
import WebSocketProvider from "../components/WebSocketProvider/WebSocketProvider";

const HomePage = () => {
  return (
    <WebSocketProvider>
      <div className={"shopping-list-container"}>
        <ShoppingList />
      </div>
    </WebSocketProvider>
  );
};

export default HomePage;
