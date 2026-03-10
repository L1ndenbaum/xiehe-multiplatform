
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { messagesData } from '../../mocks/messages';

export default function Messages() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState(messagesData);

  const handleDelete = (id: string) => {
    setMessages(messages.filter(msg => msg.id !== id));
  };

  const getTypeIcon = (type: string) => {
    switch(type) {
      case '系统通知': return { icon: 'ri-notification-3-line', color: 'from-violet-400 to-violet-500' };
      case '审核提醒': return { icon: 'ri-file-search-line', color: 'from-amber-400 to-orange-500' };
      default: return { icon: 'ri-message-3-line', color: 'from-slate-400 to-slate-500' };
    }
  };

  return (
    <div className="flex flex-col h-full bg-gradient-to-b from-slate-50 to-slate-100">
      {/* 顶部导航栏 */}
      <div className="sticky top-0 z-10 bg-gradient-to-r from-violet-600 via-purple-500 to-violet-600 px-4 pt-12 pb-6 flex-shrink-0">
        <div className="flex items-center">
          <button 
            onClick={() => navigate(-1)}
            className="w-10 h-10 flex items-center justify-center bg-white/20 backdrop-blur-sm rounded-full text-white mr-3"
          >
            <i className="ri-arrow-left-line text-lg"></i>
          </button>
          <div>
            <h1 className="text-white text-xl font-bold">消息中心</h1>
            <p className="text-violet-100 text-xs mt-0.5">{messages.length} 条消息</p>
          </div>
        </div>
      </div>

      {/* 可滚动内容区域 */}
      <div className="flex-1 overflow-y-auto px-4 py-4">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <div className="w-20 h-20 bg-slate-100 rounded-full flex items-center justify-center mb-4">
              <i className="ri-inbox-line text-slate-300 text-4xl"></i>
            </div>
            <p className="text-slate-400 text-sm">暂无消息</p>
          </div>
        ) : (
          <div className="space-y-3">
            {messages.map((message) => {
              const typeStyle = getTypeIcon(message.type);
              return (
                <div 
                  key={message.id} 
                  className="bg-white rounded-2xl p-4 shadow-lg shadow-slate-200/50 border border-slate-100"
                >
                  <div className="flex items-start">
                    <div className={`w-11 h-11 rounded-xl flex items-center justify-center mr-3 flex-shrink-0 bg-gradient-to-br ${typeStyle.color} shadow-md`}>
                      <i className={`${typeStyle.icon} text-white text-lg`}></i>
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-xs font-semibold text-slate-500 bg-slate-100 px-2 py-0.5 rounded-full">{message.type}</span>
                        <span className="text-xs text-slate-400">{message.time}</span>
                      </div>
                      <h3 className="text-sm font-bold text-slate-800 mb-1">{message.title}</h3>
                      <p className="text-xs text-slate-500 line-clamp-2">{message.content}</p>
                    </div>
                  </div>
                  <div className="flex justify-end mt-3 pt-3 border-t border-slate-50">
                    <button
                      onClick={() => handleDelete(message.id)}
                      className="px-4 py-1.5 text-xs text-red-500 hover:bg-red-50 rounded-lg transition-colors flex items-center gap-1"
                    >
                      <i className="ri-delete-bin-line"></i>
                      删除
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
