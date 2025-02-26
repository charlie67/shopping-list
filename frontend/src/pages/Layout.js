import {Outlet} from "react-router-dom";
import Header from "../components/Header/Header";
import WebSocketProvider from "../components/WebSocketProvider/WebSocketProvider";

const Layout = () => {
    return (
        <>
            <Header/>
            <div className={"content-area"}>
                <WebSocketProvider>
                  <Outlet/>
                </WebSocketProvider>
            </div>
        </>
    )
};

export default Layout;
