import { Outlet } from "react-router-dom";
import Header from "../components/Header/Header";
import Sidebar from "../components/Sidebar/Sidebar";

const Layout = () => {
  return (
    <>
      <Header/>
      <div id={"outer-container"}>
        <Sidebar/>
        <main id={"page-wrap"}>
          <Outlet />
        </main>
      </div>
    </>
  )
};

export default Layout;
