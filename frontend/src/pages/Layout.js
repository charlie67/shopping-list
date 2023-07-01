import {Outlet} from "react-router-dom";
import Header from "../components/Header/Header";

const Layout = () => {
    return (
        <>
            <Header/>
            <div className={"content-area"}>
                <Outlet/>
            </div>
        </>
    )
};

export default Layout;
