import { useNavigate } from 'react-router-dom';
import { statsData, pendingTasks, recentActivities, quickActions } from '../../mocks/dashboard';

const imageTypeColors: Record<string, string> = {
  '正面': 'bg-teal-50 text-teal-600',
  '侧面': 'bg-orange-50 text-orange-600',
  '左侧曲位': 'bg-emerald-50 text-emerald-600',
  '右侧曲位': 'bg-sky-50 text-sky-600',
  '体态照片': 'bg-rose-50 text-rose-600',
};

const priorityConfig: Record<string, { label: string; color: string }> = {
  high: { label: '紧急', color: 'bg-red-500' },
  medium: { label: '普通', color: 'bg-orange-400' },
  low: { label: '常规', color: 'bg-slate-300' },
};

export default function Dashboard() {
  const navigate = useNavigate();

  const today = new Date();
  const dateStr = today.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric', weekday: 'long' });

  return (
    <div className="flex flex-col h-full bg-slate-50">
      {/* 顶部 Header */}
      <div
        className="sticky top-0 z-20 px-4 pt-12 pb-6 flex-shrink-0"
        style={{ background: 'linear-gradient(135deg, #7c3aed 0%, #6d28d9 50%, #5b21b6 100%)' }}
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-11 h-11 rounded-2xl bg-white/20 backdrop-blur-sm flex items-center justify-center overflow-hidden border-2 border-white/30">
              <i className="ri-user-fill text-white text-xl"></i>
            </div>
            <div>
              <p className="text-violet-200 text-xs">早上好 👋</p>
              <h1 className="text-white text-base font-bold leading-tight">张医生</h1>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <div className="text-right mr-1">
              <p className="text-white/90 text-xs font-medium">{dateStr}</p>
              <p className="text-violet-200 text-[10px]">脊柱影像分析系统</p>
            </div>
            <button
              onClick={() => navigate('/messages')}
              className="w-10 h-10 flex items-center justify-center bg-white/20 backdrop-blur-sm rounded-full text-white relative"
            >
              <i className="ri-notification-3-line text-lg"></i>
              <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-red-400 rounded-full border border-violet-600"></span>
            </button>
          </div>
        </div>
      </div>

      {/* 可滚动内容区域 */}
      <div className="flex-1 overflow-y-auto px-4 py-5 space-y-5">
        {/* 数据统计横向滚动卡片 */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-sm font-bold text-slate-700">数据概览</h2>
          </div>
          <div className="flex gap-3 overflow-x-auto pb-1 -mx-4 px-4 scrollbar-hide" style={{ scrollbarWidth: 'none' }}>
            {statsData.map((stat, index) => (
              <div
                key={index}
                className={`flex-shrink-0 w-36 rounded-2xl p-4 bg-gradient-to-br ${stat.gradient} shadow-lg ${stat.shadow}`}
              >
                <div className="w-9 h-9 rounded-xl bg-white/20 flex items-center justify-center mb-3">
                  <i className={`${stat.icon} text-white text-lg`}></i>
                </div>
                <div className="text-2xl font-bold text-white leading-none mb-1">{stat.value}</div>
                <div className="text-white/80 text-[11px]">{stat.label}</div>
              </div>
            ))}
          </div>
        </div>

        {/* 快捷入口 */}
        <div>
          <h2 className="text-sm font-bold text-slate-700 mb-3">快捷入口</h2>
          <div className="grid grid-cols-4 gap-2">
            {quickActions.map((action, index) => (
              <button
                key={index}
                onClick={() => navigate(action.route)}
                className="flex flex-col items-center gap-2"
              >
                <div className={`w-14 h-14 rounded-2xl bg-gradient-to-br ${action.color} flex items-center justify-center shadow-md`}>
                  <i className={`${action.icon} text-white text-xl`}></i>
                </div>
                <span className="text-[11px] text-slate-600 font-medium">{action.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* 待处理任务 */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
          <div className="px-4 py-3.5 border-b border-slate-50 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <div className="w-1 h-4 bg-gradient-to-b from-violet-500 to-purple-600 rounded-full"></div>
              <h2 className="text-sm font-bold text-slate-800">待处理任务</h2>
            </div>
            <div className="flex items-center gap-2">
              <span className="px-2 py-0.5 bg-orange-50 text-orange-500 text-[11px] font-semibold rounded-full">
                {pendingTasks.length} 项待审
              </span>
            </div>
          </div>

          <div className="divide-y divide-slate-50">
            {pendingTasks.map((task) => {
              const priority = priorityConfig[task.priority];
              const typeColor = imageTypeColors[task.imageType] || 'bg-slate-50 text-slate-500';
              return (
                <div
                  key={task.id}
                  className="px-4 py-3.5 flex items-center gap-3 hover:bg-slate-50/60 transition-colors"
                >
                  <div className="flex flex-col items-center gap-1 flex-shrink-0">
                    <div className={`w-2 h-2 rounded-full ${priority.color}`}></div>
                  </div>
                  <div className="w-10 h-10 bg-gradient-to-br from-violet-100 to-purple-100 rounded-xl flex items-center justify-center flex-shrink-0">
                    <i className="ri-user-line text-violet-500 text-base"></i>
                  </div>
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-sm font-semibold text-slate-800">{task.patientName}</span>
                      <span className={`px-1.5 py-0.5 rounded-md text-[10px] font-medium ${typeColor}`}>
                        {task.imageType}
                      </span>
                    </div>
                    <div className="flex items-center gap-1.5 text-[11px] text-slate-400">
                      <span>{task.patientId}</span>
                      <span>·</span>
                      <span>{task.createTime}</span>
                      <span>·</span>
                      <span className={`font-medium ${task.priority === 'high' ? 'text-red-400' : task.priority === 'medium' ? 'text-orange-400' : 'text-slate-400'}`}>
                        {priority.label}
                      </span>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate(`/image-annotation?id=${task.id}`)}
                    className="flex-shrink-0 w-9 h-9 bg-gradient-to-br from-violet-500 to-purple-600 rounded-xl flex items-center justify-center shadow-md shadow-violet-300/40"
                  >
                    <i className="ri-arrow-right-line text-white text-base"></i>
                  </button>
                </div>
              );
            })}
          </div>

          <div className="px-4 py-3 border-t border-slate-50">
            <button
              onClick={() => navigate('/images')}
              className="w-full py-2.5 rounded-xl border border-violet-200 text-violet-600 text-xs font-semibold flex items-center justify-center gap-1.5 hover:bg-violet-50 transition-colors"
            >
              <span>查看全部影像</span>
              <i className="ri-arrow-right-line text-sm"></i>
            </button>
          </div>
        </div>

        {/* 近期活动 */}
        <div className="bg-white rounded-2xl shadow-sm border border-slate-100 overflow-hidden">
          <div className="px-4 py-3.5 border-b border-slate-50 flex items-center gap-2">
            <div className="w-1 h-4 bg-gradient-to-b from-sky-400 to-blue-500 rounded-full"></div>
            <h2 className="text-sm font-bold text-slate-800">近期动态</h2>
          </div>
          <div className="divide-y divide-slate-50">
            {recentActivities.map((activity) => (
              <div key={activity.id} className="px-4 py-3.5 flex items-center gap-3">
                <div className={`w-9 h-9 rounded-xl ${activity.bgColor} flex items-center justify-center flex-shrink-0`}>
                  <i className={`${activity.icon} ${activity.iconColor} text-base`}></i>
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-slate-700 leading-tight">{activity.title}</p>
                  <p className="text-[11px] text-slate-400 mt-0.5 truncate">{activity.desc}</p>
                </div>
                <span className="text-[10px] text-slate-400 flex-shrink-0">{activity.time}</span>
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* 底部导航栏 */}
      <div className="sticky bottom-0 bg-white/95 backdrop-blur-lg border-t border-slate-100 px-2 py-2 grid grid-cols-4 z-30 flex-shrink-0">
        <button className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 bg-gradient-to-br from-violet-500 to-purple-600 rounded-xl flex items-center justify-center shadow-lg shadow-violet-400/30">
            <i className="ri-dashboard-fill text-white text-lg"></i>
          </div>
          <span className="text-[10px] mt-1 font-semibold text-violet-600">工作台</span>
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
        <button onClick={() => navigate('/profile')} className="flex flex-col items-center justify-center py-1.5">
          <div className="w-10 h-10 flex items-center justify-center">
            <i className="ri-user-line text-slate-400 text-xl"></i>
          </div>
          <span className="text-[10px] mt-1 text-slate-400">个人中心</span>
        </button>
      </div>
    </div>
  );
}
