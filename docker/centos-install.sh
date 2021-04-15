sudo yum -y install git
sudo yum -y install zsh

sudo sh -c "$(curl -fsSL https://raw.github.com/robbyrussell/oh-my-zsh/master/tools/install.sh)"
sudo su -
sudo sed -i "/ZSH_THEME=/c\ZSH_THEME=\"agnoster\"" ~/.zshrc

git clone https://github.com/zsh-users/zsh-syntax-highlighting.git ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-syntax-highlighting
git clone https://github.com/zsh-users/zsh-autosuggestions ${ZSH_CUSTOM:-~/.oh-my-zsh/custom}/plugins/zsh-autosuggestions

sudo sed -i "/ZSH_THEME=/c\ZSH_THEME=\"agnoster\"" ~/.zshrc
sudo sed -i "/plugins=(git)/c\plugins=(\n\tgit\n\tzsh-syntax-highlighting\n\tzsh-autosuggestions\n)" ~/.zshrc
sudo sed -i "/ZSH=/c\ZSH=\"$HOME/.oh-my-zsh\"" ~/.zshrc

source ~/.zshrc

echo "sudo su -" >> /home/centos/.bash_profile

sudo yum install -y yum-utils
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo curl -L "https://github.com/docker/compose/releases/download/1.29.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod 755 /usr/local/bin/docker-compose

# ssh -i notifications.pem centos@133.186.247.62
