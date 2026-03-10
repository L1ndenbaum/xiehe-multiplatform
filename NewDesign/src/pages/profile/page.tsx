
import { useNavigate } from 'react-router-dom';

export default function Profile() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('isLoggedIn');
    navigate('/login');
  };

  const menuItems = [
    { icon: 'ri-user-line', label: '个人信息', path: '/profile/info', color: 'from-violet-400 to-violet-500' },
    { icon: 'ri-building-line', label: '组织管理', path: '/profile/organization', color: 'from-purple-400 to-purple-500' },
    { icon: 'ri-lock-password-line', label: '修改密码', path: '/profile/password', color: 'from-amber-400 to-amber-500' },
    { icon: 'ri-settings-3-line', label: '系统设置', path: '/profile/settings', color: 'from-emerald-400 to-emerald-500' }
  ];

  return (
    <div className="flex flex-col h-full bg-gradient-to-b from-slate-50 to-slate-100">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-10 bg-gradient-to-r from-violet-600 via-purple-500 to-violet-600 px-4 pt-12 pb-6 flex-shrink-0 rounded-b-[2rem]">
        <div className="flex items-center justify-between">
          <h1 className="text-white text-xl font-bold">个人中心</h1>
          <button 
            onClick={() => navigate('/messages')}
            className="w-10 h-10 flex items-center justify-center bg-white/20 backdrop-blur-sm rounded-full text-white relative"
          >
            <i className="ri-notification-3-line text-lg"></i>
            <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-violet-500"></span>
          </button>
        </div>
      </div>

      {/* 可滚动内容区域 */}
      <div className="flex-1 overflow-y-auto px-4 pt-4 pb-4">
        {/* 用户信息卡片 */}
        <div className="bg-white rounded-2xl p-5 shadow-xl shadow-slate-200/50 border border-slate-100 mb-4">
          <div className="flex items-center">
            <div className="w-16 h-16 bg-gradient-to-br from-violet-400 to-purple-500 rounded-2xl flex items-center justify-center mr-4 shadow-lg shadow-violet-500/30">
              <i className="ri-user-line text-white text-2xl"></i>
            </div>
            <div className="flex-1">
              <h2 className="text-lg font-bold text-slate-800 mb-1">admin</h2>
              <div className="flex items-center gap-2">
                <span className="px-3 py-1 bg-gradient-to-r from-violet-500 to-purple-500 text-white text-xs font-semibold rounded-full shadow-sm">
                  医生
                </span>
                <span className="px-3 py-1 bg-emerald-50 text-emerald-600 text-xs font-semibold rounded-full">
                  已认证
                </span>
              </div>
            </div>
            <button className="w-9 h-9 bg-slate-50 rounded-xl flex items-center justify-center">
              <i className="ri-qr-code-line text-slate-500"></i>
            </button>
          </div>
          
          <div className="grid grid-cols-3 gap-3 mt-5 pt-5 border-t border-slate-100">
            <div className="text-center">
              <div className="text-xl font-bold text-slate-800">128</div>
              <div className="text-xs text-slate-500 mt-0.5">管理患者</div>
            </div>
            <div className="text-center border-x border-slate-100">
              <div className="text-xl font-bold text-slate-800">56</div>
              <div className="text-xs text-slate-500 mt-0.5">本月审核</div>
            </div>
            <div className="text-center">
              <div className="text-xl font-bold text-slate-800">98%</div>
              <div className="text-xs text-slate-500 mt-0.5">完成率</div>
            </div>
          </div>
        </div>

        {/* 功能菜单 */}
        <div className="bg-white rounded-2xl shadow-lg shadow-slate-200/50 border border-slate-100 overflow-hidden mb-4">
          {menuItems.map((item, index) => (
            <button
              key={index}
              onClick={() => navigate(item.path)}
              className={`w-full px-4 py-4 flex items-center justify-between hover:bg-slate-50 transition-colors ${
                index !== menuItems.length - 1 ? 'border-b border-slate-50' : ''
              }`}
            >
              <div className="flex items-center">
                <div className={`w-10 h-10 bg-gradient-to-br ${item.color} rounded-xl flex items-center justify-center mr-3 shadow-md`}>
                  <i className={`${item.icon} text-white text-lg`}></i>
                </div>
                <span className="text-sm font-medium text-slate-700">{item.label}</span>
              </div>
              <i className="ri-arrow-right-s-line text-slate-300 text-xl"></i>
            </button>
          ))}
        </div>

        {/* 退出登录按钮 */}
        <button
          onClick={handleLogout}
          className="w-full py-3.5 bg-white text-red-500 rounded-2xl text-sm font-semibold shadow-lg shadow-slate-200/50 border border-slate-100 hover:bg-red-50 transition-colors flex items-center justify-center gap-2"
        >
          <i className="ri-logout-box-r-line"></i>
          退出登录
        </button>
      </div>

      {/* 底部导航栏 */}
      <div className="sticky bottom-0 bg-white/90 backdrop-blur-lg border-t border-slate-100 px-2 py-2 grid grid-cols-4 z-30 flex-shrink-0">
        <button onClick={() => navigate('/dashboard')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-dashboard-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">工作台</span>
        </button>
        <button onClick={() => navigate('/patients')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-user-heart-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">患者中心</span>
        </button>
        <button onClick={() => navigate('/images')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-image-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">影像中心</span>
        </button>
        <button className="flex flex-col items-center justify-center py-1.5 relative">
          <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-purple-500 rounded-xl flex items-center justify-center shadow-lg shadow-violet-500/30">
            <i className="ri-user-fill text-white text-lg"></i>
          </div>
          <span className="text-[10px] mt-1 font-semibold text-violet-600">个人中心</span>
        </button>
      </div>
    </div>
  );
}
